package me.ayydxn.orbit.natives.jni;

import me.ayydxn.orbit.natives.NativeBackendType;
import me.ayydxn.orbit.natives.OrbitNativeBackend;
import me.ayydxn.orbit.natives.jni.ultralight.JNIUltralightNatives;
import me.ayydxn.orbit.natives.ultralight.UltralightNatives;
import org.jetbrains.annotations.NotNull;

public final class JNINativeBackend implements OrbitNativeBackend
{
	@Override
	public void initialize()
	{
	}

	@Override
	public @NotNull NativeBackendType getType()
	{
		return NativeBackendType.JNI;
	}

	@Override
	public @NotNull UltralightNatives ultralight()
	{
		return new JNIUltralightNatives();
	}
}
