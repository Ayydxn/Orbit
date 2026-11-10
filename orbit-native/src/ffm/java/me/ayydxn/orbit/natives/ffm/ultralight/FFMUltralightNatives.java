package me.ayydxn.orbit.natives.ffm.ultralight;

import me.ayydxn.orbit.natives.ffm.utils.NativeFunctionSymbol;
import me.ayydxn.orbit.natives.ultralight.UltralightNatives;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;

public final class FFMUltralightNatives implements UltralightNatives
{
	private final NativeFunctionSymbol ulVersionString;
	private final NativeFunctionSymbol ulWebKitVersionString;

	public FFMUltralightNatives()
	{
		this.ulVersionString = NativeFunctionSymbol.of("ulVersionString", FunctionDescriptor.of(ValueLayout.ADDRESS));
		this.ulWebKitVersionString = NativeFunctionSymbol.of("ulWebKitVersionString", FunctionDescriptor.of(ValueLayout.ADDRESS));
	}

	@Override
	public String versionString()
	{
		return this.ulVersionString.getString();
	}

	@Override
	public String webkitVersionString()
	{
		return this.ulWebKitVersionString.getString();
	}
}
