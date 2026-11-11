package me.ayydxn.orbit.natives.ultralight;

public interface ULStringNatives
{
	long createString(String string);

	void destroyString(long handle);

	String getData(long handle);

	long getLength(long handle);

	boolean isEmpty(long handle);
}
