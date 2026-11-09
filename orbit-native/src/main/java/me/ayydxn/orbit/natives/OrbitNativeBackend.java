package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.exceptions.NativeBackendException;

public interface OrbitNativeBackend
{
	void initialize() throws NativeBackendException;

	NativeBackendType getType();
}
