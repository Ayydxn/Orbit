package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.ultralight.ULConfigNatives;
import me.ayydxn.orbit.natives.ultralight.ULPlatformNatives;
import me.ayydxn.orbit.natives.ultralight.ULStringNatives;
import me.ayydxn.orbit.natives.ultralight.UltralightNatives;
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
    ULPlatformNatives ulPlatform();
}
