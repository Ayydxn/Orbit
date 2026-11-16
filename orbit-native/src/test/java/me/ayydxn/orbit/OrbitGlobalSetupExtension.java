package me.ayydxn.orbit;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.OrbitNativeBackend;
import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.jni.JNINativeBackend;
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
		for (String ultralightLibrary : ULTRALIGHT_LIBRARIES_LOAD_ORDER)
			System.loadLibrary(ultralightLibrary);

        System.loadLibrary(JNI_BRIDGE_LIBRARY_NAME);

        OrbitNativeBackend orbitNativeBackend = new JNINativeBackend();
		orbitNativeBackend.initialize();

		NativeBackendManager.install(new RecordingNativeBackend(orbitNativeBackend));
	}
}
