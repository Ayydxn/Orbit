package me.ayydxn.orbit.natives.jni.ultralight;

import me.ayydxn.orbit.natives.ultralight.ULConfigNatives;

public final class JNIULConfigNatives implements ULConfigNatives
{
	@Override
	public long create()
	{
		return NativeMethods.nulCreateConfig();
	}

	@Override
	public void destroy(long handle)
	{
		NativeMethods.nulDestroyConfig(handle);
	}

	@Override
	public void setCachePath(long handle, long cachePathULString)
	{
		NativeMethods.nulConfigSetCachePath(handle, cachePathULString);
	}

	@Override
	public void setResourcePathPrefix(long handle, long resourcePathPrefixULString)
	{
		NativeMethods.nulConfigSetResourcePathPrefix(handle, resourcePathPrefixULString);
	}

	@Override
	public void setBitmapAlignment(long handle, int bitmapRowAlignment)
	{
		NativeMethods.nulConfigSetBitmapAlignment(handle, bitmapRowAlignment);
	}

    @Override
    public void setEffectQuality(long handle, int effectQualityNativeValue)
    {
        NativeMethods.nulConfigSetEffectQuality(handle, effectQualityNativeValue);
    }

    @Override
    public void setUserStylesheet(long handle, long userStylesheetULString)
    {
        NativeMethods.nulConfigSetUserStylesheet(handle, userStylesheetULString);
    }

    @Override
    public void setDeveloperMode(long handle, boolean enableDeveloperMode)
    {
        NativeMethods.nulConfigSetDeveloperMode(handle, enableDeveloperMode);
    }

    private static final class NativeMethods
	{
		private static native long nulCreateConfig();

		private static native void nulDestroyConfig(long handle);

		private static native void nulConfigSetCachePath(long configHandle, long cachePathULStringHandle);

		private static native void nulConfigSetResourcePathPrefix(long configHandle, long resourcePathPrefixULStringHandle);

		private static native void nulConfigSetBitmapAlignment(long configHandle, int bitmapRowAlignment);

        private static native void nulConfigSetEffectQuality(long configHandle, int effectQualityNativeValue);

        private static native void nulConfigSetUserStylesheet(long configHandle, long userStylesheetStringHandle);

        private static native void nulConfigSetDeveloperMode(long configHandle, boolean enableDeveloperMode);
	}
}
