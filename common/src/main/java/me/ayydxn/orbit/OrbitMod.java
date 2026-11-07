package me.ayydxn.orbit;

import dev.architectury.platform.Platform;
import me.ayydxn.orbit.utils.NativeLibraryLoader;
import me.ayydxn.orbit.utils.OrbitConstants;
import me.ayydxn.orbit.utils.OrbitNativePlatform;
import me.ayydxn.orbit.utils.ultralight.UltralightLibraryDownloader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

@Environment(EnvType.CLIENT)
public class OrbitMod
{
    private static OrbitMod INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger("Orbit UI");
    public static final String MOD_ID = "orbit";

    private OrbitMod()
    {
        try
        {
            if (!this.areUltralightLibrariesPresent())
                UltralightLibraryDownloader.downloadAndExtract(OrbitConstants.ORBIT_DIRECTORY);
        }
        catch (IOException exception)
        {
            LOGGER.error("Failed to download the Ultralight libraries", exception);
        }

        NativeLibraryLoader.load(OrbitConstants.ORBIT_DIRECTORY);
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

    private boolean areUltralightLibrariesPresent()
    {
        if (!Files.exists(OrbitConstants.ORBIT_DIRECTORY))
            return false;

        return OrbitNativePlatform.getCurrent().getUltralightLibraryFiles().stream()
                .map(OrbitConstants.ORBIT_DIRECTORY::resolve)
                .allMatch(path -> Files.exists(path) && Files.isRegularFile(path));
    }
}
