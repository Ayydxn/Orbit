package me.ayydxn.orbit.natives.jni.ultralight;

import me.ayydxn.orbit.natives.ultralight.ULRendererNatives;

public final class JNIULRendererNatives implements ULRendererNatives
{
    @Override
    public long create(long configHandle)
    {
        return NativeMethods.nulCreateRenderer(configHandle);
    }

    @Override
    public void destroy(long handle)
    {
        NativeMethods.nulDestroyRenderer(handle);
    }

    @Override
    public void update(long handle)
    {
        NativeMethods.nulUpdate(handle);
    }

    @Override
    public void refreshDisplay(long handle, int displayID)
    {
        NativeMethods.nulRefreshDisplay(handle, displayID);
    }

    @Override
    public void render(long handle)
    {
        NativeMethods.nulRender(handle);
    }

    @Override
    public String getMemoryUsage(long handle)
    {
        return NativeMethods.nulGetMemoryUsage(handle);
    }

    private static final class NativeMethods
    {
        private static native long nulCreateRenderer(long configHandle);

        private static native void nulDestroyRenderer(long handle);

        private static native void nulUpdate(long handle);

        private static native void nulRefreshDisplay(long handle, int displayID);

        private static native void nulRender(long handle);

        private static native String nulGetMemoryUsage(long handle);
    }
}
