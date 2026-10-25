package me.ayydxn.orbit.utils;

import me.ayydxn.orbit.utils.ultralight.UltralightPlatform;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLibraryLoader
{
    private static volatile boolean areLibrariesLoaded = false;

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
        for (String ultralightLibrary : UltralightPlatform.getCurrentPlatform().getLibraryFiles())
        {
            Path ultralightLibraryPath = librariesFolder.resolve(ultralightLibrary);

            System.load(ultralightLibraryPath.toString());
        }
    }

    private static void loadJNIBridgeLibrary(@NotNull Path librariesPath)
    {
        // First, try to load the JNI bridge from our classpath resources folder.
        String platform = getCurrentPlatformString();
        String libraryFilename = getPlatformLibraryFilename("OrbitJNIBridge");
        String resourcePath = "/natives/" + platform + "/" + libraryFilename;

        try (InputStream inputStream = NativeLibraryLoader.class.getResourceAsStream(resourcePath))
        {
            if (inputStream != null)
            {
                Path targetDestination = librariesPath.resolve(libraryFilename);

                if (!Files.exists(targetDestination))
                    Files.copy(inputStream, targetDestination, StandardCopyOption.REPLACE_EXISTING);

                System.load(targetDestination.toAbsolutePath().toString());

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
    }

    private static String getCurrentPlatformString()
    {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win"))
            return "windows-x64";

        if (os.contains("mac"))
            return "macos-arm64";

        return "linux-x64";
    }

    private static String getPlatformLibraryFilename(String baseName)
    {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win"))
            return baseName + ".dll";

        if (os.contains("mac"))
            return "lib" + baseName + ".dylib";

        return "lib" + baseName + ".so";
    }
}
