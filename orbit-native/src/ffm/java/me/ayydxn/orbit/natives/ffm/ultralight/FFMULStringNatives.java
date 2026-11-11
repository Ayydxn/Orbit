package me.ayydxn.orbit.natives.ffm.ultralight;

import me.ayydxn.orbit.natives.ffm.utils.NativeFunctionSymbol;
import me.ayydxn.orbit.natives.ultralight.ULStringNatives;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class FFMULStringNatives implements ULStringNatives
{
	private final NativeFunctionSymbol ulCreateString;
	private final NativeFunctionSymbol ulDestroyString;
	private final NativeFunctionSymbol ulStringGetData;
	private final NativeFunctionSymbol ulStringGetLength;
	private final NativeFunctionSymbol ulStringIsEmpty;

	public FFMULStringNatives()
	{
		this.ulCreateString = NativeFunctionSymbol.of("ulCreateString", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
		this.ulDestroyString = NativeFunctionSymbol.of("ulDestroyString", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
		this.ulStringGetData = NativeFunctionSymbol.of("ulStringGetData", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
		this.ulStringGetLength = NativeFunctionSymbol.of("ulStringGetLength", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
		this.ulStringIsEmpty = NativeFunctionSymbol.of("ulStringIsEmpty", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));
	}

	@Override
	public long createString(String string)
	{
		MemorySegment nativeString = Arena.ofAuto().allocateFrom(string);

		return this.ulCreateString.getPointer(nativeString).address();
	}

	@Override
	public void destroyString(long handle)
	{
		this.ulDestroyString.invoke(MemorySegment.ofAddress(handle));
	}

	@Override
	public String getData(long handle)
	{
		return this.ulStringGetData.getString(MemorySegment.ofAddress(handle));
	}

	@Override
	public long getLength(long handle)
	{
		return this.ulStringGetLength.getLong(MemorySegment.ofAddress(handle));
	}

	@Override
	public boolean isEmpty(long handle)
	{
		return this.ulStringIsEmpty.getBoolean(MemorySegment.ofAddress(handle));
	}
}
