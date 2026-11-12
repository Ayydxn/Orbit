package me.ayydxn.orbit.natives.ultralight;

public interface ULConfigNatives
{
	long create();

	void destroy(long handle);

	void setCachePath(long handle, long cachePathULString);

	void setResourcePathPrefix(long handle, long resourcePathPrefixULString);

	void setBitmapAlignment(long handle, int bitmapRowAlignment);

    void setEffectQuality(long handle, int effectQualityNativeValue);

    void setUserStylesheet(long handle, long userStylesheetULString);

    void setDeveloperMode(long handle, boolean enableDeveloperMode);
}
