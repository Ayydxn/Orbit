package me.ayydxn.orbit.natives.ffm.ultralight;

import me.ayydxn.orbit.natives.ffm.utils.NativeFunctionSymbol;
import me.ayydxn.orbit.natives.ultralight.ULConfigNatives;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class FFMULConfigNatives implements ULConfigNatives
{
	private final NativeFunctionSymbol ulCreateConfig;
	private final NativeFunctionSymbol ulDestroyConfig;
	private final NativeFunctionSymbol ulConfigSetCachePath;
	private final NativeFunctionSymbol ulConfigSetResourcePathPrefix;
	private final NativeFunctionSymbol ulConfigSetBitmapAlignment;
    private final NativeFunctionSymbol ulConfigSetEffectQuality;
    private final NativeFunctionSymbol ulConfigSetUserStylesheet;
    private final NativeFunctionSymbol ulConfigSetDeveloperMode;

	public FFMULConfigNatives()
	{
		this.ulCreateConfig = NativeFunctionSymbol.of("ulCreateConfig", FunctionDescriptor.of(ValueLayout.ADDRESS));
		this.ulDestroyConfig = NativeFunctionSymbol.of("ulDestroyConfig", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
		this.ulConfigSetCachePath = NativeFunctionSymbol.of("ulConfigSetCachePath", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
		this.ulConfigSetResourcePathPrefix = NativeFunctionSymbol.of("ulConfigSetResourcePathPrefix", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
		this.ulConfigSetBitmapAlignment = NativeFunctionSymbol.of("ulConfigSetBitmapAlignment", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        this.ulConfigSetEffectQuality = NativeFunctionSymbol.of("ulConfigSetEffectQuality", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        this.ulConfigSetUserStylesheet = NativeFunctionSymbol.of("ulConfigSetUserStylesheet", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        this.ulConfigSetDeveloperMode = NativeFunctionSymbol.of("ulConfigSetDeveloperMode", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));
    }

	@Override
	public long create()
	{
		return this.ulCreateConfig.getPointer().address();
	}

	@Override
	public void destroy(long handle)
	{
		this.ulDestroyConfig.invoke(MemorySegment.ofAddress(handle));
	}

	@Override
	public void setCachePath(long handle, long cachePathULString)
	{
		MemorySegment handleMemorySegment = MemorySegment.ofAddress(handle);
		MemorySegment cachePathMemorySegment = MemorySegment.ofAddress(cachePathULString);

		this.ulConfigSetCachePath.invoke(handleMemorySegment, cachePathMemorySegment);
	}

	@Override
	public void setResourcePathPrefix(long handle, long resourcePathPrefixULString)
	{
		MemorySegment handleMemorySegment = MemorySegment.ofAddress(handle);
		MemorySegment resourcePathPrefixMemorySegment = MemorySegment.ofAddress(resourcePathPrefixULString);

		this.ulConfigSetResourcePathPrefix.invoke(handleMemorySegment, resourcePathPrefixMemorySegment);
	}

	@Override
	public void setBitmapAlignment(long handle, int bitmapRowAlignment)
	{
		this.ulConfigSetBitmapAlignment.invoke(MemorySegment.ofAddress(handle), bitmapRowAlignment);
	}

    @Override
    public void setEffectQuality(long handle, int effectQualityNativeValue)
    {
		this.ulConfigSetEffectQuality.invoke(MemorySegment.ofAddress(handle), effectQualityNativeValue);
    }

    @Override
    public void setUserStylesheet(long handle, long userStylesheetULString)
    {
        MemorySegment handleMemorySegment = MemorySegment.ofAddress(handle);
        MemorySegment userStylesheetPrefixMemorySegment = MemorySegment.ofAddress(userStylesheetULString);

        this.ulConfigSetUserStylesheet.invoke(handleMemorySegment, userStylesheetPrefixMemorySegment);
    }

    @Override
    public void setDeveloperMode(long handle, boolean enableDeveloperMode)
    {
        this.ulConfigSetDeveloperMode.invoke(MemorySegment.ofAddress(handle), enableDeveloperMode);
    }
}
