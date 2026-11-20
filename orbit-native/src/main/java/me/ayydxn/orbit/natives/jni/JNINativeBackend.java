package me.ayydxn.orbit.natives.jni;

import me.ayydxn.orbit.natives.OrbitNativeBackend;
import me.ayydxn.orbit.natives.jni.ultralight.*;
import me.ayydxn.orbit.natives.ultralight.*;
import org.jetbrains.annotations.NotNull;

public final class JNINativeBackend implements OrbitNativeBackend
{
	@Override
	public void initialize()
	{
	}

    @Override
    public @NotNull UltralightNatives ultralight()
    {
        return new JNIUltralightNatives();
    }

    @Override
	public @NotNull ULStringNatives ulString()
	{
		return new JNIULStringNatives();
	}

	@Override
	public @NotNull ULConfigNatives ulConfig()
	{
		return new JNIULConfigNatives();
	}

    @Override
    public @NotNull ULRendererNatives ulRenderer()
    {
        return new JNIULRendererNatives();
    }

    @Override
    public @NotNull ULPlatformNatives ulPlatform()
    {
        return new JNIULPlatformNatives();
    }
}
