package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import static org.junit.jupiter.api.Assertions.*;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class NativeBackendManagerTest
{
	private String originalBackendProperty;

	@BeforeEach
	void clearBackendProperty()
	{
		this.originalBackendProperty = System.getProperty(NativeBackendManager.BACKEND_PROPERTY);

		System.clearProperty(NativeBackendManager.BACKEND_PROPERTY);
	}

	@AfterEach
	void restoreBackendProperty()
	{
		if (this.originalBackendProperty == null)
		{
			System.clearProperty(NativeBackendManager.BACKEND_PROPERTY);
		}
		else
		{
			System.setProperty(NativeBackendManager.BACKEND_PROPERTY, this.originalBackendProperty);
		}
	}

	@Test
	void defaultSelectionPrefersFfmWhereItIsPresentAndFallsBackToJni() throws NativeBackendException
	{
		NativeBackendType expectedType = NativeBackendManagerTest.isFfmBackendPresent() ? NativeBackendType.FFM : NativeBackendType.JNI;

		assertEquals(expectedType, NativeBackendManager.select().getType());
	}

	@Test
	void aBlankPropertyIsTreatedAsUnset() throws NativeBackendException
	{
		NativeBackendType defaultType = NativeBackendManager.select().getType();

		System.setProperty(NativeBackendManager.BACKEND_PROPERTY, "   ");

		assertEquals(defaultType, NativeBackendManager.select().getType());
	}

	@Test
	void forcingJniSelectsIt() throws NativeBackendException
	{
		System.setProperty(NativeBackendManager.BACKEND_PROPERTY, "jni");

		assertEquals(NativeBackendType.JNI, NativeBackendManager.select().getType());
	}

	@Test
	void theForcedNameIsCaseInsensitiveAndTrimmed() throws NativeBackendException
	{
		System.setProperty(NativeBackendManager.BACKEND_PROPERTY, "  jNi ");

		assertEquals(NativeBackendType.JNI, NativeBackendManager.select().getType());
	}

	@Test
	void forcingFfmSelectsItWhereItIsPresentAndFailsWithoutFallingBackWhereItIsNot() throws NativeBackendException
	{
		System.setProperty(NativeBackendManager.BACKEND_PROPERTY, "ffm");

		if (NativeBackendManagerTest.isFfmBackendPresent())
		{
			assertEquals(NativeBackendType.FFM, NativeBackendManager.select().getType());

			return;
		}

		NativeBackendException exception = assertThrows(NativeBackendException.class, NativeBackendManager::select);

		assertEquals(1, exception.getSuppressed().length);
		assertInstanceOf(ClassNotFoundException.class, exception.getSuppressed()[0].getCause());
	}

	@Test
	void anUnknownBackendFailsInsteadOfBeingIgnored()
	{
		System.setProperty(NativeBackendManager.BACKEND_PROPERTY, "bogus");

		NativeBackendException exception = assertThrows(NativeBackendException.class, NativeBackendManager::select);

		assertTrue(exception.getMessage().contains("bogus"));
	}

	@Test
	void backendTypesAreLookedUpByName()
	{
		assertEquals(NativeBackendType.JNI, NativeBackendType.fromName("JNI").orElseThrow());
		assertEquals(NativeBackendType.FFM, NativeBackendType.fromName("ffm").orElseThrow());
		assertTrue(NativeBackendType.fromName("panama").isEmpty());
	}

	@Test
	void onlyTheJniBackendNeedsTheJniBridge()
	{
		assertTrue(NativeBackendType.JNI.usesJNIBridge());
		assertFalse(NativeBackendType.FFM.usesJNIBridge());
	}

	private static boolean isFfmBackendPresent()
	{
		try
		{
			Class.forName(NativeBackendType.FFM.getBackendClassName(), false, NativeBackendManager.class.getClassLoader());

			return true;
		}
		catch (ClassNotFoundException exception)
		{
			return false;
		}
	}
}
