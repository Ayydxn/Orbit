package me.ayydxn.orbit;

import dev.architectury.platform.Platform;
import me.ayydxn.orbit.utils.NativeLibraryLoader;
import me.ayydxn.orbit.utils.OrbitConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class OrbitMod
{
    private static OrbitMod INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger("Orbit UI");
    public static final String MOD_ID = "orbit";

	private final String version;

    private OrbitMod()
    {
		this.version = Platform.getMod(MOD_ID).getVersion();

        NativeLibraryLoader.startLoading(OrbitConstants.ORBIT_DIRECTORY);
    }

    public static void initialize()
    {
        if (INSTANCE != null)
            return;

        LOGGER.info("Initializing Orbit UI... (Version: {})", Platform.getMod(MOD_ID).getVersion());

        INSTANCE = new OrbitMod();
    }

    public static OrbitMod getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Orbit UI before one was available!");

        return INSTANCE;
    }

	public String getVersion()
	{
		return this.version;
	}
}
