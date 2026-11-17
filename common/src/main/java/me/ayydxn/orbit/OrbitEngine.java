package me.ayydxn.orbit;

import me.ayydxn.orbit.ultralight.ULConfig;
import me.ayydxn.orbit.ultralight.ULPlatform;
import me.ayydxn.orbit.ultralight.ULRenderer;
import me.ayydxn.orbit.ultralight.platform.impl.StandardULFileSystem;
import me.ayydxn.orbit.ultralight.platform.impl.StandardULFontLoader;
import me.ayydxn.orbit.utils.OrbitConstants;

public class OrbitEngine
{
    private static boolean isInitialized = false;

    private ULRenderer ulRenderer;

    public void initialize()
    {
        if (isInitialized)
            return;

        OrbitMod.LOGGER.info("Initializing Orbit's UI engine...");

        ULPlatform.setFileSystem(new StandardULFileSystem());
        ULPlatform.setFontLoader(new StandardULFontLoader());

        // TODO: (Ayydxn) Create ULRenderer instance
        try (ULConfig config = ULConfig.create())
        {
            config.setResourcePathPrefix(OrbitConstants.ORBIT_RESOURCES_DIRECTORY + "/");
            config.setCachePath(OrbitConstants.ORBIT_CACHE_DIRECTORY.toString());

            this.ulRenderer = new ULRenderer(config);
        }

        isInitialized = true;
    }

    public void shutdown()
    {
        if (!isInitialized)
            return;

        this.ulRenderer.destroy();

        isInitialized = false;
    }

    public void update()
    {
        this.ulRenderer.update();
    }

    public void render()
    {
        this.ulRenderer.refreshDisplay(0);
        this.ulRenderer.render();
    }

    public String getRendererMemoryUsage()
    {
        return this.ulRenderer.getMemoryUsage();
    }
}
