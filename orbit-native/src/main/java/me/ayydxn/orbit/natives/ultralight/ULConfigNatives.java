package me.ayydxn.orbit.natives.ultralight;

import me.ayydxn.orbit.natives.generator.annotations.NativeContract;
import me.ayydxn.orbit.natives.generator.annotations.NativeFunction;
import me.ayydxn.orbit.natives.generator.annotations.NativePointer;
import me.ayydxn.orbit.natives.generator.annotations.NativeType;

@NativeContract(headers = {"Ultralight/CAPI/CAPI_Config.h"})
public interface ULConfigNatives
{
    @NativePointer("ULConfig")
    @NativeFunction("ulCreateConfig")
	long create();

    @NativeFunction("ulDestroyConfig")
	void destroy(@NativePointer("ULConfig") long handle);

    @NativeFunction("ulConfigSetCachePath")
	void setCachePath(@NativePointer("ULConfig") long handle, @NativePointer("ULString") long cachePathULString);

    @NativeFunction("ulConfigSetResourcePathPrefix")
	void setResourcePathPrefix(@NativePointer("ULConfig") long handle, @NativePointer("ULString") long resourcePathPrefixULString);

    @NativeFunction("ulConfigSetBitmapAlignment")
	void setBitmapAlignment(@NativePointer("ULConfig") long handle, int bitmapRowAlignment);

    @NativeFunction("ulConfigSetEffectQuality")
    void setEffectQuality(@NativePointer("ULConfig") long handle, @NativeType("ULEffectQuality") int effectQualityNativeValue);

    @NativeFunction("ulConfigSetUserStylesheet")
    void setUserStylesheet(@NativePointer("ULConfig") long handle, @NativePointer("ULString") long userStylesheetULString);

    @NativeFunction("ulConfigSetDeveloperMode")
    void setDeveloperMode(@NativePointer("ULConfig") long handle, boolean enableDeveloperMode);
}
