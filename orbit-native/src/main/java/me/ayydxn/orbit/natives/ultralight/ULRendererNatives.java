package me.ayydxn.orbit.natives.ultralight;

import me.ayydxn.orbit.natives.generator.annotations.*;

@NativeContract(headers = {"Ultralight/CAPI/CAPI_Renderer.h", "Ultralight/CAPI/CAPI_String.h"})
public interface ULRendererNatives
{
    @NativePointer("ULRenderer")
    @NativeFunction("ulCreateRenderer")
    long create(@NativePointer("ULConfig") long configHandle);

    @NativeFunction("ulDestroyRenderer")
    void destroy(@NativePointer("ULRenderer") long handle);

    @NativeFunction("ulUpdate")
    void update(@NativePointer("ULRenderer") long handle);

    @NativeFunction("ulRefreshDisplay")
    void refreshDisplay(@NativePointer("ULRenderer") long handle, @NativeType("unsigned int") int displayID);

    @NativeFunction("ulRender")
    void render(@NativePointer("ULRenderer") long handle);

    @UltralightString(owned = true)
    @NativeFunction("ulGetMemoryUsage")
    String getMemoryUsage(@NativePointer("ULRenderer") long handle);
}
