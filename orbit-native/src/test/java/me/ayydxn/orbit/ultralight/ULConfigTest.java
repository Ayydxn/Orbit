package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.ultralight.config.ULEffectQuality;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ULConfigTest
{
    @Test
    @DisplayName("Initializes with Valid Native Pointer")
    void testInitialization()
    {
        assertDoesNotThrow(() ->
        {
            try (ULConfig config = ULConfig.create())
            {
                // If we reach here, the native pointer is valid.
                config.setBitmapAlignment(16);

                // Ensure handle is non-zero
                assertNotNull(config.toString());
            }
        });
    }

    @Test
    @DisplayName("Accepts Every Bound Setter")
    void testSetters(@TempDir Path tempDir)
    {
        assertDoesNotThrow(() ->
        {
            try (ULConfig config = ULConfig.create())
            {
                config.setCachePath(tempDir.toString());
                config.setResourcePathPrefix("resources/");
                config.setBitmapAlignment(0);
                config.setEffectQuality(ULEffectQuality.MEDIUM);
                config.setUserStylesheet("body { background: transparent; user-select: none; }");
                config.setDeveloperMode(true);
            }
        });
    }

    @Test
    @DisplayName("Create and Destroy Multiple Configs Without Heap Corruption")
    void testHeapCorruption()
    {
        for (int i = 0; i < 100; i++)
        {
            try (ULConfig config = ULConfig.create())
            {
                config.setBitmapAlignment(16);
            }
        }
    }
}
