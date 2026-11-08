package me.ayydxn.orbit.utils;

import dev.architectury.platform.Platform;
import me.ayydxn.orbit.OrbitMod;
import me.ayydxn.orbit.platform.PlatformResourceLocator;
import me.ayydxn.orbit.utils.ultralight.UltralightLibraryDownloader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class NativeLibraryLoader
{
	private static final CompletableFuture<NativeLibraryLoadStatus> LOAD_FUTURE = new CompletableFuture<>();

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.followRedirects(HttpClient.Redirect.NORMAL)
		.connectTimeout(Duration.ofSeconds(10))
		.build();

	private static final String RELEASE_CHECKSUM_URL = "https://github.com/Ayydxn/Orbit/releases/download/v%s/%s.sha256";
	private static final String DISABLE_INTEGRITY_CHECK_PROPERTY = "orbit.disableIntegrityCheck";

	private static volatile NativeLibraryLoadStatus nativeLibraryLoadStatus = NativeLibraryLoadStatus.notLoaded();

	private NativeLibraryLoader()
	{
	}

	public static synchronized void startLoading(@NotNull Path librariesPath)
	{
		if (nativeLibraryLoadStatus.state() != NativeLibraryLoadStatus.State.NOT_LOADED)
			return;

		nativeLibraryLoadStatus = NativeLibraryLoadStatus.loading();

		// Getting this stuff on the main thread so that we don't have to on our worker thread, which could be dangerous.
		LibraryLoadContext loadContext = new LibraryLoadContext(librariesPath, Platform.getMod(OrbitMod.MOD_ID).getVersion(), Platform.isDevelopmentEnvironment());

		Thread libraryLoaderThread = new Thread(() -> NativeLibraryLoader.runLoad(loadContext), "Orbit-NativeLibraryLoader");
		libraryLoaderThread.setDaemon(true);
		libraryLoaderThread.start();
	}

	private static void runLoad(LibraryLoadContext loadContext)
	{
		NativeLibraryLoadStatus result;

		try
		{
			result = NativeLibraryLoader.loadLibraries(loadContext);
		}
		catch (NativeLibraryLoadException exception)
		{
			OrbitMod.LOGGER.error("Failed to load Orbit's native libraries ({}): {}", exception.getFailureReason(), exception.getMessage(), exception.getCause());

			result = NativeLibraryLoadStatus.failed(exception.getFailureReason());
		}
		catch (Throwable throwable)
		{
			OrbitMod.LOGGER.error("Unexpected failure while loading Orbit's native libraries", throwable);

			result = NativeLibraryLoadStatus.failed(NativeLibraryLoadStatus.FailureReason.UNKNOWN);
		}

		// The status has to be set before the future completes, so anything waiting in the future sees a consistent state.
		nativeLibraryLoadStatus = result;

		LOAD_FUTURE.complete(result);
	}

	private static NativeLibraryLoadStatus loadLibraries(LibraryLoadContext loadContext) throws NativeLibraryLoadException
	{
		OrbitNativePlatform nativePlatform = NativeLibraryLoader.detectPlatform();

		NativeLibraryLoader.ensureUltralightLibrariesPresent(loadContext.librariesPath, nativePlatform);

		// Everything that can fail verification happens before any native code is loaded.
		byte[] jniBridgeLibraryBytes = NativeLibraryLoader.readEmbeddedJNIBridgeLibrary(nativePlatform);
		boolean wasJNIBridgeLibraryVerified = NativeLibraryLoader.verifyJNIBridgeLibrary(jniBridgeLibraryBytes, nativePlatform);
		Path jniBridgeLibraryPath = NativeLibraryLoader.writeJNIBridgeLibrary(loadContext.librariesPath, nativePlatform, jniBridgeLibraryBytes);

		for (String ultralightLibrary : nativePlatform.getUltralightLibraryFiles())
			NativeLibraryLoader.loadLibrary(loadContext.librariesPath.resolve(ultralightLibrary));

		OrbitMod.LOGGER.info("Successfully loaded Ultralight libraries!");

		NativeLibraryLoader.loadLibrary(jniBridgeLibraryPath);

		OrbitMod.LOGGER.info("Successfully loaded JNI bridge library!");

		return wasJNIBridgeLibraryVerified ? NativeLibraryLoadStatus.loaded() : NativeLibraryLoadStatus.loadedUnverified();
	}

	private static OrbitNativePlatform detectPlatform() throws NativeLibraryLoadException
	{
		try
		{
			return OrbitNativePlatform.getCurrent();
		}
		catch (IllegalStateException exception)
		{
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.UNSUPPORTED_PLATFORM, exception.getMessage(), exception);
		}
	}

	private static void ensureUltralightLibrariesPresent(Path librariesPath, OrbitNativePlatform nativePlatform) throws NativeLibraryLoadException
	{
		if (NativeLibraryLoader.areUltralightLibrariesPresent(librariesPath, nativePlatform))
			return;

		try
		{
			UltralightLibraryDownloader.downloadAndExtract(librariesPath);
		}
		catch (IOException exception)
		{
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.ULTRALIGHT_DOWNLOAD_FAILED, "Failed to download the Ultralight libraries", exception);
		}
	}

	private static boolean areUltralightLibrariesPresent(Path librariesPath, OrbitNativePlatform nativePlatform)
	{
		if (!Files.exists(librariesPath))
			return false;

		return nativePlatform.getUltralightLibraryFiles().stream()
			.map(librariesPath::resolve)
			.allMatch(path -> Files.exists(path) && Files.isRegularFile(path));
	}

	private static byte[] readEmbeddedJNIBridgeLibrary(OrbitNativePlatform nativePlatform) throws NativeLibraryLoadException
	{
		String resourcePath = "/natives/" + nativePlatform.getID() + "/" + nativePlatform.getJNIBridgeLibraryFilename();

		try (InputStream inputStream = PlatformResourceLocator.getResourceAsStream(resourcePath))
		{
			if (inputStream == null)
				throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.JNI_BRIDGE_MISSING, "Orbit's JNI bridge was not found in the mod at " + resourcePath, null);

			return inputStream.readAllBytes();
		}
		catch (IOException exception)
		{
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED, "Failed to read Orbit's JNI bridge from " + resourcePath, exception);
		}
	}

	private static boolean verifyJNIBridgeLibrary(byte[] jniBridgeLibraryBytes, OrbitNativePlatform nativePlatform) throws NativeLibraryLoadException
	{
		if (Platform.isDevelopmentEnvironment())
			return false;

		if (Boolean.getBoolean(DISABLE_INTEGRITY_CHECK_PROPERTY))
		{
			OrbitMod.LOGGER.warn("Orbit's integrity check has been disabled through the -D{} property. The JNI bridge was not verified!",
				DISABLE_INTEGRITY_CHECK_PROPERTY);

			return false;
		}

		String expectedChecksum = NativeLibraryLoader.fetchReleaseChecksum(nativePlatform.getJNIBridgeLibraryFilename());

		// We hash the bytes inside the mod itself rather than a file on disk, so what we verify is exactly what we then write and load.
		if (!NativeLibraryLoader.computeSHA256(jniBridgeLibraryBytes).equals(expectedChecksum))
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.JNI_BRIDGE_INAUTHENTIC, "Orbit's JNI bridge failed its integrity check!", null);

		return true;
	}

	private static Path writeJNIBridgeLibrary(Path librariesPath, OrbitNativePlatform nativePlatform, byte[] jniBridgeLibraryBytes) throws NativeLibraryLoadException
	{
		Path targetDestination = librariesPath.resolve(nativePlatform.getJNIBridgeLibraryFilename());

		try
		{
			// An identical file is left alone. Windows won't let us overwrite a DLL that another game instance has loaded.
			if (Files.isRegularFile(targetDestination) && Arrays.equals(Files.readAllBytes(targetDestination), jniBridgeLibraryBytes))
				return targetDestination;

			Files.write(targetDestination, jniBridgeLibraryBytes);

			return targetDestination;
		}
		catch (IOException exception)
		{
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED, "Failed to extract Orbit's JNI bridge to " + targetDestination, exception);
		}
	}

	private static void loadLibrary(Path libraryPath) throws NativeLibraryLoadException
	{
		try
		{
			System.load(libraryPath.toAbsolutePath().toString());
		}
		catch (UnsatisfiedLinkError exception)
		{
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED, "Failed to load native library: " + libraryPath.getFileName(), exception);
		}
	}

	private static String fetchReleaseChecksum(String libraryFilename) throws NativeLibraryLoadException
	{
		String modVersion = Platform.getMod(OrbitMod.MOD_ID).getVersion();
		String url = String.format(RELEASE_CHECKSUM_URL, modVersion, libraryFilename);

		HttpRequest httpRequest = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("User-Agent", "Orbit-NativeLibraryLoader")
			.timeout(Duration.ofSeconds(15))
			.GET()
			.build();

		try
		{
			HttpResponse<String> httpResponse = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (httpResponse.statusCode() != 200)
				throw new IOException("HTTP " + httpResponse.statusCode());

			String checksum = httpResponse.body().trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
			if (!checksum.matches("[0-9a-f]{64}"))
				throw new IOException("Response was not a valid SHA-256 hash");

			return checksum;
		}
		catch (IOException exception)
		{
			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.INTEGRITY_CHECK_UNAVAILABLE, "Could not fetch the release checksum for Orbit's JNI bridge from " + url, exception);
		}
		catch (InterruptedException exception)
		{
			Thread.currentThread().interrupt();

			throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.INTEGRITY_CHECK_UNAVAILABLE, "Interrupted while fetching Orbit's JNI bridge checksum", exception);
		}
	}

	private static String computeSHA256(byte[] bytes)
	{
		try
		{
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		}
		catch (NoSuchAlgorithmException exception)
		{
			throw new IllegalStateException("SHA-256 is unavailable!", exception);
		}
	}

	public static NativeLibraryLoadStatus getLoadStatus()
	{
		return nativeLibraryLoadStatus;
	}

	private static final class NativeLibraryLoadException extends Exception
	{
		private final NativeLibraryLoadStatus.FailureReason failureReason;

		private NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason failureReason, String message, Throwable cause)
		{
			super(message, cause);

			this.failureReason = failureReason;
		}

		private NativeLibraryLoadStatus.FailureReason getFailureReason()
		{
			return this.failureReason;
		}
	}

	private record LibraryLoadContext(Path librariesPath, String modVersion, boolean isDevelopmentEnvironment) {}
}
