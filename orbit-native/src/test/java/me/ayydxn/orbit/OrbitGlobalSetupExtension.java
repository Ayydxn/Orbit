package me.ayydxn.orbit;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.OrbitNativeBackend;
import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.recording.RecordingNativeBackend;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class OrbitGlobalSetupExtension implements BeforeAllCallback
{
	private static final List<String> ULTRALIGHT_LIBRARIES_LOAD_ORDER = List.of(
		"UltralightCore",
		"WebCore",
		"Ultralight"
	);

	private static final String JNI_BRIDGE_LIBRARY_NAME = "OrbitJNIBridge";

	private static boolean wereLibrariesLoaded = false;

	public static final String EXPECTED_BACKEND_PROPERTY = "orbit.test.expectedBackend";

	@Override
	public void beforeAll(@NonNull ExtensionContext extensionContext) throws NativeBackendException
	{
		synchronized (OrbitGlobalSetupExtension.class)
		{
			if (!wereLibrariesLoaded)
			{
				OrbitGlobalSetupExtension.setup();

				wereLibrariesLoaded = true;
			}
		}
	}

	private static void setup() throws NativeBackendException
	{
		OrbitNativeBackend orbitNativeBackend = NativeBackendManager.select();
		String expectedBackend = System.getProperty(EXPECTED_BACKEND_PROPERTY);

		if (expectedBackend != null && !expectedBackend.equalsIgnoreCase(orbitNativeBackend.getType().getName()))
		{
			throw new IllegalStateException(String.format("These unit tests were setup to run on the %s backend, but the %s backend was selected instead!",
				expectedBackend, orbitNativeBackend.getType().getName()));
		}

		for (String ultralightLibrary : ULTRALIGHT_LIBRARIES_LOAD_ORDER)
			System.loadLibrary(ultralightLibrary);

		if (orbitNativeBackend.getType().usesJNIBridge())
			System.loadLibrary(JNI_BRIDGE_LIBRARY_NAME);

		orbitNativeBackend.initialize();

		NativeBackendManager.install(new RecordingNativeBackend(orbitNativeBackend));
	}
}
