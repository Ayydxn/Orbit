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

	public String getString()
	{
		try
		{
			MemorySegment result = (MemorySegment) this.methodHandle.invoke();
			if (result.equals(MemorySegment.NULL))
				throw new NullPointerException(String.format("The native call to %s returned null!", this.methodName));

			return result.reinterpret(Long.MAX_VALUE).getString(0L);
		}
		catch (Throwable throwable)
		{
			throw new RuntimeException(String.format("Something went wrong while trying to invoke native function '%s'!", this.methodName),
				throwable);
		}
	}
}
