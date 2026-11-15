package me.ayydxn.orbit.ultralight.platform;

import me.ayydxn.orbit.ultralight.ULPlatform;
import me.ayydxn.orbit.ultralight.font.ULFontFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ULPlatformTest
{
    @BeforeEach
    @AfterEach
    void releasePlatform()
    {
        ULPlatform.shutdown();
    }

    @Test
    @DisplayName("Setting a Logger Doesn't Throw")
    void testSetLogger()
    {
        assertDoesNotThrow(() -> ULPlatform.setLogger(new FakeLogger()));
    }

    @Test
    @DisplayName("Setting a File System Doesn't Throw")
    void testSetFileSystem()
    {
        assertDoesNotThrow(() -> ULPlatform.setFileSystem(new FakeFileSystem()));
    }

    @Test
    @DisplayName("Setting a Font Loader Doesn't Throw")
    void testSetFontLoader()
    {
        assertDoesNotThrow(() -> ULPlatform.setFontLoader(new FakeFontLoader()));
    }

    @Test
    @DisplayName("All Three Platform Objects Can Be Installed Together In Any Order")
    void testInstallOrderDoesNotMatter()
    {
        assertDoesNotThrow(() ->
        {
            ULPlatform.setLogger(new FakeLogger());
            ULPlatform.setFileSystem(new FakeFileSystem());
            ULPlatform.setFontLoader(new FakeFontLoader());

            ULPlatform.shutdown();

            ULPlatform.setFontLoader(new FakeFontLoader());
            ULPlatform.setFileSystem(new FakeFileSystem());
            ULPlatform.setLogger(new FakeLogger());
        });
    }

    @Test
    @DisplayName("Replacing Platform Objects Repeatedly Doesn't Crash")
    void testRepeatedReplacement()
    {
        assertDoesNotThrow(() ->
        {
            for (int i = 0; i < 100; i++)
            {
                ULPlatform.setLogger(new FakeLogger());
                ULPlatform.setFileSystem(new FakeFileSystem());
                ULPlatform.setFontLoader(new FakeFontLoader());
            }
        });
    }

    @Test
    @DisplayName("Shutdown Is Safe With Nothing Installed And When Repeated")
    void testShutdownIsIdempotent()
    {
        assertDoesNotThrow(() ->
        {
            ULPlatform.shutdown();

            ULPlatform.setLogger(new FakeLogger());

            ULPlatform.shutdown();
            ULPlatform.shutdown();
        });
    }

    @Test
    @DisplayName("Platform Objects Can Be Installed Again After Shutdown")
    void testReinstallAfterShutdown()
    {
        assertDoesNotThrow(() ->
        {
            for (int i = 0; i < 10; i++)
            {
                ULPlatform.setLogger(new FakeLogger());
                ULPlatform.setFileSystem(new FakeFileSystem());
                ULPlatform.setFontLoader(new FakeFontLoader());

                ULPlatform.shutdown();
            }
        });
    }

    @Test
    @DisplayName("Null Platform Objects Are Rejected Cleanly With IllegalArgumentException")
    void testNullRejection()
    {
        assertThrows(IllegalArgumentException.class, () -> ULPlatform.setLogger(null));
        assertThrows(IllegalArgumentException.class, () -> ULPlatform.setFileSystem(null));
        assertThrows(IllegalArgumentException.class, () -> ULPlatform.setFontLoader(null));
    }

    @Test
    @DisplayName("A Rejected Null Doesn't Disturb An Installed Platform Object")
    void testNullRejectionKeepsExistingInstall()
    {
        ULPlatform.setLogger(new FakeLogger());

        assertThrows(IllegalArgumentException.class, () -> ULPlatform.setLogger(null));
        assertDoesNotThrow(ULPlatform::shutdown);
    }

    @Test
    @DisplayName("Concurrent Installs From Multiple Threads Don't Crash")
    void testConcurrentInstalls() throws Exception
    {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try
        {
            List<Future<?>> futures = new ArrayList<>();
            for (int thread = 0; thread < 4; thread++)
            {
                futures.add(executorService.submit(() ->
                {
                    for (int i = 0; i < 50; i++)
                    {
                        ULPlatform.setLogger(new FakeLogger());
                        ULPlatform.setFileSystem(new FakeFileSystem());
                        ULPlatform.setFontLoader(new FakeFontLoader());
                    }
                }));
            }

            for (Future<?> future : futures)
                future.get(30, TimeUnit.SECONDS);
        }
        finally
        {
            executorService.shutdownNow();
        }
    }

    @Test
    @DisplayName("Replaced Platform Objects Are Released")
    void testReplacedObjectsAreReleased() throws InterruptedException
    {
        WeakReference<?>[] first = ULPlatformTest.installAll();

        ULPlatformTest.installAll();

        ULPlatformTest.assertAllCollected(first);
    }

    @Test
    @DisplayName("Shutdown Releases Every Installed Platform Object")
    void testShutdownReleasesObjects() throws InterruptedException
    {
        WeakReference<?>[] installed = ULPlatformTest.installAll();

        ULPlatform.shutdown();

        ULPlatformTest.assertAllCollected(installed);
    }

    // Returns weak references to the logger, file system and font loader, in that order. Nothing here keeps a strong reference to them.
    private static WeakReference<?>[] installAll()
    {
        FakeLogger logger = new FakeLogger();
        FakeFileSystem fileSystem = new FakeFileSystem();
        FakeFontLoader fontLoader = new FakeFontLoader();

        ULPlatform.setLogger(logger);
        ULPlatform.setFileSystem(fileSystem);
        ULPlatform.setFontLoader(fontLoader);

        return new WeakReference<?>[] { new WeakReference<>(logger), new WeakReference<>(fileSystem), new WeakReference<>(fontLoader) };
    }

    private static void assertAllCollected(WeakReference<?>[] references) throws InterruptedException
    {
        String[] names = { "ULLogger", "ULFileSystem", "ULFontLoader" };

        List<String> retained = new ArrayList<>();
        for (int attempt = 0; attempt < 50; attempt++)
        {
            System.gc();

            retained.clear();
            for (int i = 0; i < references.length; i++)
            {
                if (references[i].get() != null)
                    retained.add(names[i]);
            }

            if (retained.isEmpty())
                return;

            Thread.sleep(20);
        }

        fail("Still referenced after GC: " + retained);
    }

    // These are real classes rather than lambdas because a non-capturing lambda is a cached singleton that never becomes collectable.
    private static final class FakeLogger implements ULLogger
    {
        @Override
        public void logMessage(String message, Level logLevel)
        {
        }
    }

    private static final class FakeFileSystem implements ULFileSystem
    {
        @Override
        public boolean fileExists(String path)
        {
            return false;
        }

        @Override
        public String getFileMimeType(String path)
        {
            return "text/plain";
        }

        @Override
        public String getFileCharset(String path)
        {
            return "utf-8";
        }

        @Override
        public byte[] openFile(String path)
        {
            return null;
        }
    }

    private static final class FakeFontLoader implements ULFontLoader
    {
        @Override
        public String getFallbackFont()
        {
            return "Arial";
        }

        @Override
        public String getFallbackFontForCharacters(String characters, int weight, boolean isItalic)
        {
            return "Arial";
        }

        @Override
        public ULFontFile load(String family, int weight, boolean isItalic)
        {
            return null;
        }
    }
}
