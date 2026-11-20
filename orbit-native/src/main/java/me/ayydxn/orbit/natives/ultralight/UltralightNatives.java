package me.ayydxn.orbit.natives.ultralight;

import me.ayydxn.orbit.natives.generator.annotations.NativeString;
import me.ayydxn.orbit.natives.generator.annotations.NativeContract;
import me.ayydxn.orbit.natives.generator.annotations.NativeFunction;

@NativeContract(headers = {"Ultralight/CAPI/CAPI_Defines.h"})
public interface UltralightNatives
{
    @NativeString
    @NativeFunction("ulVersionString")
	String versionString();

    @NativeString
    @NativeFunction(value = "ulWebKitVersionString")
	String webkitVersionString();
}
