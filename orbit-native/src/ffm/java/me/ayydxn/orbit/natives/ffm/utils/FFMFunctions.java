package me.ayydxn.orbit.natives.ffm.utils;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

public final class FFMFunctions
{
	private static final Linker LINKER = Linker.nativeLinker();
	private static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.loaderLookup();

	private FFMFunctions()
	{
	}

	public static MethodHandle link(String symbolName, FunctionDescriptor functionDescriptor)
	{
		MemorySegment functionSymbol = findSymbol(symbolName);

		return LINKER.downcallHandle(functionSymbol, functionDescriptor);
	}

	public static MemorySegment findSymbol(String symbolName)
	{
		return SYMBOL_LOOKUP.find(symbolName).orElseThrow(() -> new IllegalArgumentException("Couldn't find the native symbol '" + symbolName + "'"));
	}
}
