package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.exceptions.NativeBackendException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class NativeBackendManager
{
	// The order backends are tried in when none is forced.
	// We try FFM first since it has a smaller footprint, and we want to prefer it over JNI when possible.
	private static final List<NativeBackendType> DEFAULT_CANDIDATES = List.of(NativeBackendType.FFM, NativeBackendType.JNI);

	// Setting this to the name of a backend (jni or ffm) forces that backend, and no other will be tried.
	public static final String BACKEND_PROPERTY = "orbit.nativeBackend";

	private static volatile OrbitNativeBackend installedBackend;

	private NativeBackendManager()
	{
	}

	public static OrbitNativeBackend select() throws NativeBackendException
	{
		List<NativeBackendType> candidates = NativeBackendManager.getCandidates();

		NativeBackendException failure = new NativeBackendException("No native backend is available (tried: " +
			candidates.stream().map(NativeBackendType::getName).collect(Collectors.joining(", ")) + ")");

		for (NativeBackendType candidate : candidates)
		{
			try
			{
				return NativeBackendManager.instantiate(candidate);
			}
			catch (NativeBackendException exception)
			{
				failure.addSuppressed(exception);
			}
		}

		throw failure;
	}

	public static void install(OrbitNativeBackend orbitNativeBackend)
	{
		installedBackend = Objects.requireNonNull(orbitNativeBackend);
	}

	public static OrbitNativeBackend getInstance()
	{
		OrbitNativeBackend orbitNativeBackend = installedBackend;
		if (orbitNativeBackend == null)
			throw new IllegalStateException("Tried to get an instance of Orbit's current native backend before it was loaded!");

		return orbitNativeBackend;
	}

	private static OrbitNativeBackend instantiate(NativeBackendType nativeBackendType) throws NativeBackendException
	{
		try
		{
			Class<?> backendClass = Class.forName(nativeBackendType.getBackendClassName());

			return (OrbitNativeBackend) backendClass.getDeclaredConstructor().newInstance();
		}
		catch (ClassNotFoundException exception)
		{
			throw new NativeBackendException("The " + nativeBackendType.getName() + " backend isn't present in this environment", exception);
		}
		catch (ReflectiveOperationException | LinkageError exception)
		{
			throw new NativeBackendException("The " + nativeBackendType.getName() + " backend couldn't be created", exception);
		}
	}

	private static List<NativeBackendType> getCandidates() throws NativeBackendException
	{
		String requestedBackend = System.getProperty(BACKEND_PROPERTY);
		if (requestedBackend == null || requestedBackend.isBlank())
			return DEFAULT_CANDIDATES;

		Optional<NativeBackendType> forcedBackend = NativeBackendType.fromName(requestedBackend.trim());
		if (forcedBackend.isEmpty())
			throw new NativeBackendException("Unknown native backend '" + requestedBackend + "' given through -D" + BACKEND_PROPERTY + " (expected jni or ffm)");

		return List.of(forcedBackend.get());
	}
}
