package me.ayydxn.orbit.natives.ffm;

import me.ayydxn.orbit.natives.NativeBackendType;
import me.ayydxn.orbit.natives.OrbitNativeBackend;
import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.ffm.utils.FFMFunctions;

public final class FFMNativeBackend implements OrbitNativeBackend
{
	@Override
	public void initialize() throws NativeBackendException
	{
		try
		{
			// Perform a simple symbol lookup to ensure that the FFM native library is loaded and linked correctly.
			FFMFunctions.findSymbol("ulVersionString");
		}
		catch (RuntimeException exception)
		{
			throw new NativeBackendException("Failed to initialize the FFM native backend", exception);
		}
	}

	@Override
	public NativeBackendType getType()
	{
		return NativeBackendType.FFM;
	}
}
