package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.ultralight.*;
import org.jetbrains.annotations.NotNull;

public interface OrbitNativeBackend
{
	void initialize() throws NativeBackendException;

	@NotNull
	UltralightNatives ultralight();

	@NotNull
	ULStringNatives ulString();

	@NotNull
	ULConfigNatives ulConfig();

    @NotNull
    ULRendererNatives ulRenderer();

    @NotNull
    ULPlatformNatives ulPlatform();
}
