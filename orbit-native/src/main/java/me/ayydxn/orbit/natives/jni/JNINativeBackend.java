package me.ayydxn.orbit.natives.jni;

import me.ayydxn.orbit.natives.NativeBackendType;
import me.ayydxn.orbit.natives.OrbitNativeBackend;

public final class JNINativeBackend implements OrbitNativeBackend
{
	@Override
	public void initialize()
	{
	}

	@Override
	public NativeBackendType getType()
	{
		return NativeBackendType.JNI;
	}
}
