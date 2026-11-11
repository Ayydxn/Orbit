package me.ayydxn.orbit.ultralight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UltralightTest
{
	@Test
	@DisplayName("Ultralight Version String Is Returned and Not Blank")
	void ultralightVersionStringIsReturned()
	{
		String versionString = Ultralight.versionString();

		assertNotNull(versionString);
		assertFalse(versionString.isBlank());
	}

	@Test
	@DisplayName("WebKit Version String Is Returned and Not Blank")
	void webKitVersionStringIsReturned()
	{
		String webKitVersionString = Ultralight.webkitVersionString();

		assertNotNull(webKitVersionString);
		assertFalse(webKitVersionString.isBlank());
	}

	@Test
	@DisplayName("Version Strings Contain Digits")
	void versionStringsLookLikeVersions()
	{
		assertTrue(Ultralight.versionString().chars().anyMatch(Character::isDigit));
		assertTrue(Ultralight.webkitVersionString().chars().anyMatch(Character::isDigit));
	}

	// Both strings come from a C pointer, so this catches a stray terminator, trailing newline or garbage past the end of the string.
	@Test
	@DisplayName("Version Strings Don't Contain Control Characters Or Trailing Whitespaces")
	void versionStringsAreCleanText()
	{
		for (String versionString : new String[] { Ultralight.versionString(), Ultralight.webkitVersionString() })
		{
			assertEquals(versionString.strip(), versionString);
			assertTrue(versionString.chars().noneMatch(Character::isISOControl));
		}
	}

	@Test
	@DisplayName("Repeated Version String Calls Return The Same Value")
	void repeatedCallsReturnTheSameValue()
	{
		assertEquals(Ultralight.versionString(), Ultralight.versionString());
		assertEquals(Ultralight.webkitVersionString(), Ultralight.webkitVersionString());
	}

	// Ultralight and WebKit have different versions, so equal strings would mean one binding is wired to the other's function.
	@Test
	@DisplayName("Ultralight and WebKit Version Strings Are Distinct Values")
	void ultralightVersionAndWebKitArentTheSame()
	{
		assertNotEquals(Ultralight.versionString(), Ultralight.webkitVersionString());
	}
}
