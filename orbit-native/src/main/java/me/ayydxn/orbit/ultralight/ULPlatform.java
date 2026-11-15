package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.ultralight.ULPlatformNatives;
import me.ayydxn.orbit.ultralight.platform.ULFileSystem;
import me.ayydxn.orbit.ultralight.platform.ULFontLoader;
import me.ayydxn.orbit.ultralight.platform.ULLogger;

public final class ULPlatform
{
    private static final ULPlatformNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulPlatform();

    public static void setLogger(ULLogger logger)
    {
        if (logger == null)
            throw new IllegalArgumentException("Cannot set the platform logger to a null ULLogger instance!");

        NATIVES.setLogger(logger);
    }

    public static void setFileSystem(ULFileSystem fileSystem)
    {
        if (fileSystem == null)
            throw new IllegalArgumentException("Cannot set the platform filesystem to a null ULFileSystem instance!");

        NATIVES.setFileSystem(fileSystem);
    }

    public static void setFontLoader(ULFontLoader fontLoader)
    {
        if (fontLoader == null)
            throw new IllegalArgumentException("Cannot set the platform font loader to a null ULFontLoader instance!");

        NATIVES.setFontLoader(fontLoader);
    }

    public static void shutdown()
    {
        NATIVES.shutdown();
    }
}
