package me.ayydxn.orbit.natives;

import java.util.Objects;

public final class NativeBackendManager
{
	private static volatile OrbitNativeBackend installedBackend;

	private NativeBackendManager()
	{
	}

	public static void install(OrbitNativeBackend orbitNativeBackend)
	{
		installedBackend = Objects.requireNonNull(orbitNativeBackend);
	}

	public static OrbitNativeBackend getCurrentBackendInstance()
	{
		OrbitNativeBackend orbitNativeBackend = installedBackend;
		if (orbitNativeBackend == null)
			throw new IllegalStateException("Tried to get an instance of Orbit's current native backend before it was loaded!");

		return orbitNativeBackend;
	}
}
