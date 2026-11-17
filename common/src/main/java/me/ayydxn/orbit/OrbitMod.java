package me.ayydxn.orbit;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import me.ayydxn.orbit.utils.NativeLibraryLoader;
import me.ayydxn.orbit.utils.OrbitConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Environment(EnvType.CLIENT)
public class OrbitMod
{
    private static OrbitMod INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger("Orbit UI");
    public static final String MOD_ID = "orbit";

    private final OrbitEngine orbitEngine;
	private final String version;

    private OrbitMod()
    {
        this.orbitEngine = new OrbitEngine();
		this.version = Platform.getMod(MOD_ID).getVersion();

        NativeLibraryLoader.startLoading(OrbitConstants.ORBIT_DIRECTORY);

        this.extractUltralightResources();

        ClientLifecycleEvent.CLIENT_STARTED.register(client -> this.orbitEngine.initialize());

        ClientTickEvent.Client.CLIENT_POST.register(client ->
        {
            this.orbitEngine.update();
            this.orbitEngine.render();
        });

        ClientLifecycleEvent.CLIENT_STOPPING.register(client -> this.orbitEngine.shutdown());
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

    public OrbitEngine getEngine()
    {
        return this.orbitEngine;
    }

    public String getVersion()
	{
		return this.version;
	}

    private void extractUltralightResources()
    {
        // Ultralight can't load cacert.pem and icudt67l.dat from the mod's resources folder.
        // So, we have to copy them to somewhere on the disk and then point Ultralight to that.
        try
        {
            Files.createDirectories(OrbitConstants.ORBIT_RESOURCES_DIRECTORY);
        }
        catch (IOException exception)
        {
            LOGGER.error("Failed to create Ultralight resources folder!", exception);
        }

        for (String targetResourceFile : new String[] { "cacert.pem", "icudt67l.dat" })
        {
            String resourceFilePath = String.format("/assets/%s/ultralight/%s", MOD_ID, targetResourceFile);
            Path destinationFile = OrbitConstants.ORBIT_RESOURCES_DIRECTORY.resolve(targetResourceFile);

            try (InputStream inputStream = OrbitMod.class.getResourceAsStream(resourceFilePath))
            {
                if (inputStream == null)
                    throw new FileNotFoundException(String.format("Ultralight resource file '%s' was not found!",  resourceFilePath));

                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException exception)
            {
                LOGGER.error("Failed to copy Ultralight resource file!", exception);
            }
        }
    }
}
