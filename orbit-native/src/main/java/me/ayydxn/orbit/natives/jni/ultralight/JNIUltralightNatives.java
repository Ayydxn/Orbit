package me.ayydxn.orbit.natives.jni.ultralight;

import me.ayydxn.orbit.natives.ultralight.UltralightNatives;

public final class JNIUltralightNatives implements UltralightNatives
{
	@Override
	public String versionString()
	{
		return NativeMethods.nulVersionString();
	}

	@Override
	public String webkitVersionString()
	{
		return NativeMethods.nulWebKitVersionString();
	}

	private static final class NativeMethods
	{
		private static native String nulVersionString();

		private static native String nulWebKitVersionString();
	}
}
