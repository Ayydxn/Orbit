package me.ayydxn.orbit.utils;

import dev.architectury.platform.Platform;
import me.ayydxn.orbit.OrbitMod;
import me.ayydxn.orbit.platform.PlatformResourceLocator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

public final class NativeLibraryLoader
{
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL) // GitHub redirects release assets to its CDN.
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String RELEASE_CHECKSUM_URL = "https://github.com/Ayydxn/Orbit/releases/download/v%s/%s.sha256";

    private static volatile LibraryIntegrityStatus libraryIntegrityStatus = new LibraryIntegrityStatus(false, LibraryIntegrityStatus.FailureReason.UNKNOWN);
    private static volatile boolean areLibrariesLoaded = false;

	private NativeLibraryLoader()
	{
	}

    public static synchronized void load(@NotNull Path librariesPath)
    {
        if (areLibrariesLoaded)
            return;

        NativeLibraryLoader.loadUltralightLibraries(librariesPath);
        NativeLibraryLoader.loadJNIBridgeLibrary(librariesPath);

        areLibrariesLoaded = true;
    }

    private static void loadUltralightLibraries(@NotNull Path librariesFolder)
    {
        for (String ultralightLibrary : OrbitNativePlatform.getCurrent().getUltralightLibraryFiles())
        {
            Path ultralightLibraryPath = librariesFolder.resolve(ultralightLibrary);

            try
            {
                System.load(ultralightLibraryPath.toString());
            }
            catch (UnsatisfiedLinkError exception)
            {
                throw new RuntimeException("Failed to load Ultralight library: " + ultralightLibrary, exception);
            }
        }

        OrbitMod.LOGGER.info("Successfully loaded Ultralight libraries!");
    }

    private static void loadJNIBridgeLibrary(@NotNull Path librariesPath)
    {
        // First, try to load the JNI bridge from our classpath resources folder.
		OrbitNativePlatform platform = OrbitNativePlatform.getCurrent();
		String libraryFilename = platform.getJNIBridgeLibraryFileName();
		String resourcePath = "/natives/" + platform.getID() + "/" + libraryFilename;

        try (InputStream inputStream = PlatformResourceLocator.getResourceAsStream(resourcePath))
        {
            if (inputStream != null)
            {
                Path targetDestination = librariesPath.resolve(libraryFilename);

                if (Platform.isDevelopmentEnvironment())
                {
                    Files.copy(inputStream, targetDestination, StandardCopyOption.REPLACE_EXISTING);
                }
                else
                {
                    String expectedChecksum = fetchReleaseChecksum(libraryFilename);

                    if (!Files.exists(targetDestination))
                        Files.copy(inputStream, targetDestination, StandardCopyOption.REPLACE_EXISTING);

                    libraryIntegrityStatus = new LibraryIntegrityStatus(computeSHA256(targetDestination).equals(expectedChecksum),
                            LibraryIntegrityStatus.FailureReason.UNKNOWN);

                    // Of course, we don't want people running modified versions of the JNI bridge that may or not be malicious now, do we?
                    if (!libraryIntegrityStatus.isValid())
                    {
                        libraryIntegrityStatus.setFailureReason(LibraryIntegrityStatus.FailureReason.JNI_BRIDGE_INAUTHENTIC);

                        OrbitMod.LOGGER.fatal("Orbit's JNI bridge failed its integrity check: {}", targetDestination);
                    }
                }

                System.load(targetDestination.toAbsolutePath().toString());

                OrbitMod.LOGGER.info("Successfully loaded JNI bridge library!");

                return;
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Failed to extract Orbit's JNI library bridge from " + resourcePath, exception);
        }

        // If the JNI bridge was not found in the classpath, try to load it from the library path.
        try
        {
            System.loadLibrary("OrbitJNIBridge");
        }
        catch (UnsatisfiedLinkError exception)
        {
            throw new RuntimeException("Orbit's JNI bridge library was not found on classpath or library path.", exception);
        }

        OrbitMod.LOGGER.info("Successfully loaded JNI bridge library!");
    }

    private static String fetchReleaseChecksum(String libraryFilename)
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

            String checksum = httpResponse.body().trim().split("\\s+")[0].toLowerCase();
            if (!checksum.matches("[0-9a-f]{64}"))
                throw new IOException("Response was not a valid SHA-256 hash");

            return checksum;
        }
        catch (IOException exception)
        {
            throw new SecurityException("Could not fetch the release checksum for Orbit's JNI bridge from " + url, exception);
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();

            throw new SecurityException("Interrupted while fetching Orbit's JNI bridge checksum", exception);
        }
    }

    private static String computeSHA256(Path file) throws IOException
    {
        try (InputStream inputStream = Files.newInputStream(file))
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1)
                digest.update(buffer, 0, bytesRead);

            return HexFormat.of().formatHex(digest.digest());
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 is unavailable!", exception);
        }
    }

    public static LibraryIntegrityStatus getLibraryIntegrityStatus()
    {
        return libraryIntegrityStatus;
    }
}
