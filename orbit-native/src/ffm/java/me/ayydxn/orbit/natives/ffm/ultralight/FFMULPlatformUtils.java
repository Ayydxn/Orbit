package me.ayydxn.orbit.natives.ffm.ultralight;

import me.ayydxn.orbit.natives.ffm.utils.NativeFunctionSymbol;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * The handful of Ultralight functions that platform callbacks need to read arguments and build return values.
 */
final class FFMULPlatformUtils
{
	private final NativeFunctionSymbol ulCreateString = NativeFunctionSymbol.of("ulCreateString", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
	private final NativeFunctionSymbol ulDestroyString = NativeFunctionSymbol.of("ulDestroyString", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
	private final NativeFunctionSymbol ulStringGetData = NativeFunctionSymbol.of("ulStringGetData", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
	private final NativeFunctionSymbol ulStringAssignString = NativeFunctionSymbol.of("ulStringAssignString", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
	private final NativeFunctionSymbol ulCreateBufferFromCopy = NativeFunctionSymbol.of("ulCreateBufferFromCopy", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
	private final NativeFunctionSymbol ulFontFileCreateFromFilePath = NativeFunctionSymbol.of("ulFontFileCreateFromFilePath", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
	private final NativeFunctionSymbol ulFontFileCreateFromBuffer = NativeFunctionSymbol.of("ulFontFileCreateFromBuffer", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

	String readString(MemorySegment ulString)
	{
		return this.ulStringGetData.getString(ulString);
	}


	MemorySegment createString(String string)
	{
		try (Arena arena = Arena.ofConfined())
		{
			return this.ulCreateString.getPointer(arena.allocateFrom(string));
		}
	}

	void destroyString(MemorySegment ulString)
	{
		this.ulDestroyString.invoke(ulString);
	}

	void assignString(MemorySegment destination, String value)
	{
		MemorySegment source = this.createString(value);

		try
		{
			this.ulStringAssignString.invoke(destination, source);
		}
		finally
		{
			this.destroyString(source);
		}
	}

	MemorySegment createBuffer(byte[] data)
	{
		try (Arena arena = Arena.ofConfined())
		{
			MemorySegment nativeData = arena.allocate(data.length);
			nativeData.copyFrom(MemorySegment.ofArray(data));

			return this.ulCreateBufferFromCopy.getNullablePointer(nativeData, (long) data.length);
		}
	}

	MemorySegment createFontFileFromFilePath(String filePath)
	{
		MemorySegment nativePath = this.createString(filePath);

		try
		{
			return this.ulFontFileCreateFromFilePath.getNullablePointer(nativePath);
		}
		finally
		{
			this.destroyString(nativePath);
		}
	}

	MemorySegment createFontFileFromBuffer(MemorySegment buffer)
	{
		return this.ulFontFileCreateFromBuffer.getNullablePointer(buffer);
	}
}
