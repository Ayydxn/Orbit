package me.ayydxn.orbit.natives.jni.ultralight;

import me.ayydxn.orbit.natives.ultralight.ULStringNatives;

public final class JNIULStringNatives implements ULStringNatives
{
	@Override
	public long createString(String string)
	{
		return NativeMethods.nulCreateString(string);
	}

	@Override
	public void destroyString(long handle)
	{
		NativeMethods.nulDestroyString(handle);
	}

	@Override
	public String getData(long handle)
	{
		return NativeMethods.nulStringGetData(handle);
	}

	@Override
	public long getLength(long handle)
	{
		return NativeMethods.nulStringGetLength(handle);
	}

	@Override
	public boolean isEmpty(long handle)
	{
		return NativeMethods.nulStringIsEmpty(handle);
	}

	private static final class NativeMethods
	{
		private static native long nulCreateString(String string);

		private static native void nulDestroyString(long handle);

		private static native String nulStringGetData(long handle);

		private static native long nulStringGetLength(long handle);

		private static native boolean nulStringIsEmpty(long handle);
	}
}
