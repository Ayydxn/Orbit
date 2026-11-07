package me.ayydxn.orbit.utils;

import dev.architectury.platform.Platform;

import java.nio.file.Path;

public final class OrbitConstants
{
	private OrbitConstants()
	{
	}

    public static final Path ORBIT_DIRECTORY = Platform.getGameFolder().resolve("orbit");
}
