package me.ayydxn.orbit.utils.ultralight;

import net.minecraft.Util;

import java.util.List;

public enum UltralightPlatform
{
    WINDOWS(
            "Windows",
            "win-x64",
            "https://github.com/Ayydxn/Ultralight-SDKs/releases/download/2.0.0-beta.2/ul-sdk-2.0.0-beta.2-win-x64-free.zip",
            List.of("UltralightCore.dll", "WebCore.dll", "Ultralight.dll")
    ),

    MAC_OS("MacOS",
            "mac-x64",
            "https://github.com/Ayydxn/Ultralight-SDKs/releases/download/2.0.0-beta.2/ul-sdk-2.0.0-beta.2-mac-arm64-free.zip",
            List.of("libUltralightCore.dylib", "libWebCore.dylib", "libUltralight.dylib")
    ),

    LINUX("Linux",
            "linux-x64",
            "https://github.com/Ayydxn/Ultralight-SDKs/releases/download/2.0.0-beta.2/ul-sdk-2.0.0-beta.2-linux-x64-free.zip",
            List.of("libUltralightCore.so", "libWebCore.so", "libUltralight.so")
    );

    private final String name;
    private final String shortName;
    private final String sdkDownloadURL;
    private final List<String> libraryFiles;

    UltralightPlatform(String name, String shortName, String sdkDownloadURL, List<String> libraryFiles)
    {
        this.name = name;
        this.shortName = shortName;
        this.sdkDownloadURL = sdkDownloadURL;
        this.libraryFiles = libraryFiles;
    }

    public static UltralightPlatform getCurrentPlatform()
    {
        return switch (Util.getPlatform())
        {
            case WINDOWS -> UltralightPlatform.WINDOWS;
            case OSX -> UltralightPlatform.MAC_OS;
            case LINUX -> UltralightPlatform.LINUX;
            default -> throw new IllegalStateException("Current platform is unsupported by Ultralight!");
        };
    }

    public String getName()
    {
        return this.name;
    }

    public String getShortName()
    {
        return this.shortName;
    }

    public String getSDKDownloadURL()
    {
        return this.sdkDownloadURL;
    }

    public List<String> getLibraryFiles()
    {
        return this.libraryFiles;
    }
}
