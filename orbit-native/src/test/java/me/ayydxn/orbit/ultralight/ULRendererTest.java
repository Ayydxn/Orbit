package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.ultralight.platform.impl.StandardULFileSystem;
import me.ayydxn.orbit.ultralight.platform.impl.StandardULFontLoader;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ULRendererTest
{
    private ULConfig config;
    private ULRenderer renderer;

    @BeforeAll
    @BeforeEach
    void setUp()
    {
        ULPlatform.setFileSystem(new StandardULFileSystem());
        ULPlatform.setFontLoader(new StandardULFontLoader());

        URL ultralightResourcesURL = Objects.requireNonNull(ULRendererTest.class.getClassLoader().getResource("./ultralight"));

        this.config = ULConfig.create();

        try
        {
            config.setResourcePathPrefix(new File(ultralightResourcesURL.toURI()) + "/");
        }
        catch (URISyntaxException exception)
        {
            exception.printStackTrace();
        }

        this.renderer = new ULRenderer(this.config);
    }

    @AfterAll
    void tearDown()
    {
        this.renderer.destroy();
        this.config.close();
    }

    @Test
    @Order(1)
    void createDoesNotThrow()
    {
        assertNotNull(this.renderer);
    }

    @Test
    @Order(2)
    void updateDoesNotThrow()
    {
        assertDoesNotThrow(() -> this.renderer.update());
    }

    @Test
    @Order(3)
    void updateCanBeCalledRepeatedly()
    {
        assertDoesNotThrow(() ->
        {
            for (int i = 0; i < 100; i++)
                this.renderer.update();
        });
    }

    @Test
    @Order(4)
    void refreshDisplayDoesNotThrow()
    {
        assertDoesNotThrow(() -> this.renderer.refreshDisplay(0));
    }

    @Test
    @Order(5)
    void refreshDisplayAcceptsArbitraryDisplayID()
    {
        assertDoesNotThrow(() -> this.renderer.refreshDisplay(42));
    }

    @Test
    @Order(6)
    void renderDoesNotThrow()
    {
        assertDoesNotThrow(() -> this.renderer.render());
    }

    @Test
    @Order(7)
    void updateAndRenderLoopDoesNotThrow()
    {
        assertDoesNotThrow(() ->
        {
            for (int i = 0; i < 10; i++)
            {
                this.renderer.update();
                this.renderer.refreshDisplay(0);
                this.renderer.render();
            }
        });
    }

    @Test
    @Order(8)
    void getMemoryUsageReturnsNonEmptyString()
    {
        String memoryUsage = this.renderer.getMemoryUsage();

        assertNotNull(memoryUsage);
        assertFalse(memoryUsage.isEmpty());
    }

    @Test
    @Order(9)
    void getMemoryUsageIsStableAcrossCalls()
    {
        // Exercises the native-side ulDestroyString cleanup path repeatedly to catch double-free/leak crashes.
        assertDoesNotThrow(() ->
        {
            for (int i = 0; i < 50; i++)
                assertNotNull(this.renderer.getMemoryUsage());
        });
    }

    @Test
    @Order(100)
    void destroyDoesNotThrow()
    {
        assertDoesNotThrow(() -> this.renderer.destroy());
    }

    @Test
    @Order(101)
    void destroyIsIdempotent()
    {
        // The handle is zeroed after the first destroy, so further calls must be no-ops rather than a double free.
        assertDoesNotThrow(() ->
        {
            this.renderer.destroy();
            this.renderer.destroy();
        });
    }
}
