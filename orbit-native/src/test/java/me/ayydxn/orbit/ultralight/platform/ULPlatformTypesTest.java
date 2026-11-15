package me.ayydxn.orbit.ultralight.platform;

import me.ayydxn.orbit.ultralight.font.ULFontFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ULPlatformTypesTest
{
    @Test
    @DisplayName("Every Log Level Round-Trips Through Its Native Value")
    void testLevelRoundTrip()
    {
        for (ULLogger.Level level : ULLogger.Level.values())
            assertEquals(level, ULLogger.Level.createFromNativeValue(level.getNativeValue()));
    }

    @Test
    @DisplayName("Every Log Level Has A Distinct Native Value")
    void testLevelNativeValuesAreDistinct()
    {
        Set<Integer> nativeValues = new HashSet<>();

        for (ULLogger.Level level : ULLogger.Level.values())
            assertTrue(nativeValues.add(level.getNativeValue()), level + " shares its native value with another level");
    }

    @Test
    @DisplayName("Unknown Native Log Levels Fall Back To INFO")
    void testUnknownLevelFallsBack()
    {
        assertEquals(ULLogger.Level.INFO, ULLogger.Level.createFromNativeValue(-1));
        assertEquals(ULLogger.Level.INFO, ULLogger.Level.createFromNativeValue(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("Font File From A File Path Reports Its Source And Path")
    void testFontFileFromFilePath()
    {
        ULFontFile fontFile = ULFontFile.fromFilePath("fonts/Roboto.ttf");

        assertEquals(ULFontFile.Source.FILE_PATH, fontFile.getSource());
        assertEquals("fonts/Roboto.ttf", fontFile.getFilePath());
        assertNull(fontFile.getBuffer());
    }

    @Test
    @DisplayName("Font File From A Buffer Reports Its Source And Contents")
    void testFontFileFromBuffer()
    {
        byte[] data = { 1, 2, 3, 4 };
        ULFontFile fontFile = ULFontFile.fromBuffer(data);

        assertEquals(ULFontFile.Source.BUFFER, fontFile.getSource());
        assertArrayEquals(data, fontFile.getBuffer());
        assertNull(fontFile.getFilePath());
    }

    @Test
    @DisplayName("Font File Rejects Null Inputs With IllegalArgumentException")
    void testFontFileNullRejection()
    {
        assertThrows(IllegalArgumentException.class, () -> ULFontFile.fromFilePath(null));
        assertThrows(IllegalArgumentException.class, () -> ULFontFile.fromBuffer(null));
    }
}
