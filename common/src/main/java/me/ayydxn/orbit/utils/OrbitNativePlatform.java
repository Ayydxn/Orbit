package me.ayydxn.orbit.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum OrbitNativePlatform
{
	WINDOWS_X64("Windows (x64)", "windows-x64", "windows-x64", "OrbitJNIBridge.dll",
		List.of("UltralightCore.dll", "WebCore.dll", "Ultralight.dll")),

	MACOS_ARM64("macOS (Apple Silicon)", "macos-arm64", "mac-arm64", "libOrbitJNIBridge.dylib",
		List.of("libUltralightCore.dylib", "libWebCore.dylib", "libUltralight.dylib")),

	LINUX_X64("Linux (x64)", "linux-x64", "linux-x64", "libOrbitJNIBridge.so",
		List.of("libUltralightCore.so", "libWebCore.so", "libUltralight.so"));

	private static final String SDK_DOWNLOAD_URL_FORMAT = "https://ultralig.ht/api/v1/sdk/download?platform=%s&version=%s&format=json";

	private static OrbitNativePlatform currentPlatform;

	private final String name;
	private final String id;
	private final String ultralightPlatformID;
	private final String jniBridgeLibraryFileName;
	private final List<String> ultralightLibraryFiles;

	OrbitNativePlatform(String name, String id, String ultralightPlatformID, String jniBridgeLibraryFileName, List<String> ultralightLibraryFiles)
	{
		this.name = name;
		this.id = id;
		this.ultralightPlatformID = ultralightPlatformID;
		this.jniBridgeLibraryFileName = jniBridgeLibraryFileName;
		this.ultralightLibraryFiles = ultralightLibraryFiles;
	}

	public static synchronized OrbitNativePlatform getCurrent()
	{
		if (currentPlatform == null)
			currentPlatform = OrbitNativePlatform.detect(System.getProperty("os.name"), System.getProperty("os.arch"));

		return currentPlatform;
	}

	private static OrbitNativePlatform detect(String osName, String osArchitecture)
	{
		String os = osName.toLowerCase(Locale.ROOT);
		String architecture = osArchitecture.toLowerCase(Locale.ROOT);

		boolean isX64 = architecture.equals("amd64") || architecture.equals("x86_64");
		boolean isArm64 = architecture.equals("aarch64") || architecture.equals("arm64");

		if (os.startsWith("windows") && isX64)
			return WINDOWS_X64;

		if (os.contains("mac") && isArm64)
			return MACOS_ARM64;

		if (os.contains("linux") && isX64)
			return LINUX_X64;

		String supportedPlatforms = Arrays.stream(OrbitNativePlatform.values())
			.map(OrbitNativePlatform::getName)
			.collect(Collectors.joining(", "));

		throw new IllegalStateException(String.format("Orbit doesn't support this platform (OS: %s, architecture: %s). Supported platforms: %s.", osName,
			osArchitecture, supportedPlatforms));
	}

	public String getName()
	{
		return this.name;
	}

	// This matches the folder names under natives/ in the jar and the Gradle 'platform' property.
	public String getID()
	{
		return this.id;
	}

	public String getJNIBridgeLibraryFilename()
	{
		return this.jniBridgeLibraryFileName;
	}

	public List<String> getUltralightLibraryFiles()
	{
		return this.ultralightLibraryFiles;
	}

	public String getSDKDownloadURL()
	{
		return String.format(SDK_DOWNLOAD_URL_FORMAT, this.ultralightPlatformID, OrbitConstants.ULTRALIGHT_VERSION);
	}
}
