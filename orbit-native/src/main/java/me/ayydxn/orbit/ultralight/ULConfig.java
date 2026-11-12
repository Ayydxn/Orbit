package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.ultralight.ULConfigNatives;
import me.ayydxn.orbit.ultralight.config.ULEffectQuality;

public final class ULConfig implements AutoCloseable
{
	private static final ULConfigNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulConfig();

	private long handle;

	private ULConfig()
	{
		this.handle = NATIVES.create();
	}

	public static ULConfig create()
	{
		return new ULConfig();
	}

	@Override
	public void close()
	{
		if (this.handle != 0L)
		{
			NATIVES.destroy(this.handle);

			this.handle = 0L;
		}
	}

	public void setCachePath(String cachePath)
	{
		try (ULString cachePathULString = ULString.create(cachePath))
		{
			NATIVES.setCachePath(this.handle, cachePathULString.getHandle());
		}
	}

	public void setResourcePathPrefix(String resourcePathPrefix)
	{
		try (ULString resourcePathPrefixULString = ULString.create(resourcePathPrefix))
		{
			NATIVES.setResourcePathPrefix(this.handle, resourcePathPrefixULString.getHandle());
		}
	}

	public void setBitmapAlignment(int bitmapRowAlignment)
	{
		NATIVES.setBitmapAlignment(this.handle, bitmapRowAlignment);
	}

    public void setEffectQuality(ULEffectQuality effectQuality)
    {
        NATIVES.setEffectQuality(this.handle, effectQuality.getNativeValue());
    }

    public void setUserStylesheet(String userStylesheet)
    {
        try (ULString userStylesheetULString = ULString.create(userStylesheet))
        {
            NATIVES.setUserStylesheet(this.handle, userStylesheetULString.getHandle());
        }
    }

    public void setDeveloperMode(boolean enableDeveloperMode)
    {
        NATIVES.setDeveloperMode(this.handle, enableDeveloperMode);
    }
}
