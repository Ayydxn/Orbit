package me.ayydxn.orbit;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class OrbitGlobalSetupExtension implements BeforeAllCallback
{
	private static final List<String> NATIVE_LIBRARY_LOAD_ORDER = List.of(
		"UltralightCore",
		"WebCore",
		"Ultralight",
		"OrbitJNIBridge"
	);

	private static boolean wereLibrariesLoaded = false;

	@Override
	public void beforeAll(@NonNull ExtensionContext extensionContext)
	{
		synchronized (OrbitGlobalSetupExtension.class)
		{
			if (!wereLibrariesLoaded)
			{
				for (String nativeLibraryName : NATIVE_LIBRARY_LOAD_ORDER)
					System.loadLibrary(nativeLibraryName);

				wereLibrariesLoaded = true;
			}
		}
	}
}
