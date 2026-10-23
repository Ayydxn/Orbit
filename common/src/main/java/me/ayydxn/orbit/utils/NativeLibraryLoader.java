package me.ayydxn.orbit.utils;

import me.ayydxn.orbit.utils.ultralight.UltralightPlatform;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class NativeLibraryLoader
{
    private static volatile boolean areLibrariesLoaded = false;

    public static synchronized void load(@NotNull Path librariesPath)
    {
        if (areLibrariesLoaded)
            return;

        NativeLibraryLoader.loadUltralightLibraries(librariesPath);

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
}
