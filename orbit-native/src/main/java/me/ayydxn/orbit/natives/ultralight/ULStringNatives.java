package me.ayydxn.orbit.natives.ultralight;

import me.ayydxn.orbit.natives.generator.annotations.NativeContract;
import me.ayydxn.orbit.natives.generator.annotations.NativeFunction;
import me.ayydxn.orbit.natives.generator.annotations.NativePointer;

@NativeContract(headers = {"Ultralight/CAPI/CAPI_String.h"})
public interface ULStringNatives
{
    @NativePointer("ULString")
    @NativeFunction(value = "ulCreateString", custom = true)
    long createString(String string);

    @NativeFunction("ulDestroyString")
    void destroyString(@NativePointer("ULString") long handle);

    @NativeFunction(value = "ulStringGetData", custom = true)
    String getData(@NativePointer("ULString") long handle);

    @NativeFunction("ulStringGetLength")
    long getLength(@NativePointer("ULString") long handle);

    @NativeFunction("ulStringIsEmpty")
    boolean isEmpty(@NativePointer("ULString") long handle);
}
