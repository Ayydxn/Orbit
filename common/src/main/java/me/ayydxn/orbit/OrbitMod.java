package me.ayydxn.orbit;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@Environment(EnvType.CLIENT)
public class OrbitMod
{
    private static OrbitMod INSTANCE;

    public static final Logger LOGGER = (Logger) LogManager.getLogger("Orbit UI");
    public static final String MOD_ID = "orbit";

    private OrbitMod()
    {
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
}
