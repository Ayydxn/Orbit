package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.ultralight.UltralightNatives;

public final class Ultralight
{
	private static final UltralightNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ultralight();

	public static String versionString()
	{
		return NATIVES.versionString();
	}

	public static String webkitVersionString()
	{
		return NATIVES.webkitVersionString();
	}
}
