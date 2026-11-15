package me.ayydxn.orbit.ultralight.platform;

import me.ayydxn.orbit.ultralight.font.ULFontFile;

public interface ULFontLoader
{
	String getFallbackFont();

	String getFallbackFontForCharacters(String characters, int weight, boolean isItalic);

	ULFontFile load(String family, int weight, boolean isItalic);
}
