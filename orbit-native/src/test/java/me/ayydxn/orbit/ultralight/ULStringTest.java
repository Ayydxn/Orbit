package me.ayydxn.orbit.ultralight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ULStringTest
{
	@Test
	@DisplayName("Verify Basic Creation and Data Retrieval")
	void testBasicCreation()
	{
		String input = "Hello Orbit!";
		try (ULString ulString = ULString.create(input))
		{
			assertEquals(input, ulString.getData(), "Native string data should match Java input");
			assertFalse(ulString.isEmpty());
			assertEquals(input.length(), ulString.getLength());
		}
	}

	@Test
	@DisplayName("Verify Empty String Behavior")
	void testEmptyString()
	{
		try (ULString ulString = ULString.create(""))
		{
			assertTrue(ulString.isEmpty());
			assertEquals(0, ulString.getLength());
			assertEquals("", ulString.getData());
		}
	}

	@Test
	@DisplayName("Verify Handle Nullification After Close")
	void testCloseSafety()
	{
		ULString ulString = ULString.create("Temporary");
		long capturedHandle = ulString.getHandle();
		assertNotEquals(0, capturedHandle);

		ulString.close();
		assertEquals(0, ulString.getHandle(), "Handle must be zeroed out after close to prevent use-after-free");

		// Ensure double-close doesn't crash
		assertDoesNotThrow(ulString::close);
	}

	@Test
	@DisplayName("Verify Distinct Strings Get Distinct Handles")
	void testDistinctHandles()
	{
		try (ULString first = ULString.create("Same"); ULString second = ULString.create("Same"))
		{
			assertEquals(first.getData(), second.getData());
			assertNotEquals(first.getHandle(), second.getHandle(), "Each string should have a unique native handle");
		}
	}

	@Test
	@DisplayName("Verify Unicode Round-Trip")
	void testUnicodeRoundTrip()
	{
		String input = "こんにちは 🌐 مرحبا";

		try (ULString ulString = ULString.create(input))
		{
			assertEquals(input, ulString.getData());
			assertEquals(input.getBytes(StandardCharsets.UTF_8).length, ulString.getLength());
		}
	}

	@Test
	@DisplayName("Verify Null Input Is Rejected Cleanly With IllegalArgumentException")
	@SuppressWarnings("resource")
	void testNullRejection()
	{
		assertThrows(IllegalArgumentException.class, () -> ULString.create(null));
	}
}
