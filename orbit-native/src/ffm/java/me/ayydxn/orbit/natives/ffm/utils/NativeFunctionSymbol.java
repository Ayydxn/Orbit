package me.ayydxn.orbit.natives.ffm.utils;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public record NativeFunctionSymbol(MethodHandle methodHandle, String methodName)
{
	public static NativeFunctionSymbol of(String methodName, FunctionDescriptor functionDescriptor)
	{
		return new NativeFunctionSymbol(FFMFunctions.link(methodName, functionDescriptor), methodName);
	}

	public void invoke(Object... arguments)
	{
		this.invokeNativeFunction(arguments);
	}

	public long getLong(Object... arguments)
	{
		return (long) this.invokeNativeFunction(arguments);
	}

	public boolean getBoolean(Object... arguments)
	{
		return (boolean) this.invokeNativeFunction(arguments);
	}

	public String getString(Object... arguments)
	{
		return this.getPointer(arguments)
			.reinterpret(Long.MAX_VALUE)
			.getString(0L);
	}

	public MemorySegment getPointer(Object... arguments)
	{
		MemorySegment result = this.getNullablePointer(arguments);
		if (result.equals(MemorySegment.NULL))
			throw new NullPointerException(String.format("The native call to %s returned null!", this.methodName));

		return result;
	}

	public MemorySegment getNullablePointer(Object... arguments)
	{
		return (MemorySegment) this.invokeNativeFunction(arguments);
	}

	private Object invokeNativeFunction(Object... arguments)
	{
		try
		{
			return this.methodHandle.invokeWithArguments(arguments);
		}
		catch (Throwable throwable)
		{
			throw new RuntimeException(String.format("Something went wrong while trying to invoke native function '%s'!", this.methodName),
				throwable);
		}
	}
}
