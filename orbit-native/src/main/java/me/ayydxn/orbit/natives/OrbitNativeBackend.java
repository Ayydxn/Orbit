package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.ultralight.ULStringNatives;
import me.ayydxn.orbit.natives.ultralight.UltralightNatives;
import me.ayydxn.orbit.ultralight.ULString;
import org.jetbrains.annotations.NotNull;

public interface OrbitNativeBackend
{
	void initialize() throws NativeBackendException;

	@NotNull
	NativeBackendType getType();

	@NotNull
	UltralightNatives ultralight();

	@NotNull
	ULStringNatives ulString();
}
