package me.ayydxn.orbit.natives.jni.ultralight;

import me.ayydxn.orbit.natives.ultralight.ULPlatformNatives;
import me.ayydxn.orbit.ultralight.platform.ULFileSystem;
import me.ayydxn.orbit.ultralight.platform.ULFontLoader;
import me.ayydxn.orbit.ultralight.platform.ULLogger;

public final class JNIULPlatformNatives implements ULPlatformNatives
{
    @Override
    public void setLogger(ULLogger logger)
    {
        NativeMethods.nulPlatformSetLogger(logger);
    }

    @Override
    public void setFileSystem(ULFileSystem fileSystem)
    {
        NativeMethods.nulPlatformSetFileSystem(fileSystem);
    }

    @Override
    public void setFontLoader(ULFontLoader fontLoader)
    {
        NativeMethods.nulPlatformSetFontLoader(fontLoader);
    }

    @Override
    public void shutdown()
    {
        NativeMethods.nulPlatformShutdown();
    }

    private static final class NativeMethods
    {
        private static native void nulPlatformSetLogger(ULLogger logger);

        private static native void nulPlatformSetFileSystem(ULFileSystem fileSystem);

        private static native void nulPlatformSetFontLoader(ULFontLoader fontLoader);

        private static native void nulPlatformShutdown();
    }
}
