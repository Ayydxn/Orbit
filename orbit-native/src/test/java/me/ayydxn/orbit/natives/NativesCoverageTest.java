package me.ayydxn.orbit.natives;

import me.ayydxn.orbit.natives.recording.RecordingNativeBackend;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// This has to run after every other test class, which is what the @Order and the class orderer in junit-platform.properties are for.
@Order(Integer.MAX_VALUE)
class NativesCoverageTest
{
	// The Gradle test tasks turn this on. It's off for single-class runs, where most contract methods are naturally never called.
	private static final String ENFORCE_PROPERTY = "orbit.test.enforceContractCoverage";

	@Test
	void everyContractMethodWasExercisedOnThisBackend()
	{
		assumeTrue(Boolean.getBoolean(ENFORCE_PROPERTY), "Contract coverage is only enforced when the whole suite runs (-D" + ENFORCE_PROPERTY + "=true)");

		RecordingNativeBackend recordingBackend = assertInstanceOf(RecordingNativeBackend.class, NativeBackendManager.getCurrentBackendInstance(),
			"The tests weren't set up through OrbitGlobalSetupExtension");

		assertTrue(recordingBackend.getUncoveredContractMethods().isEmpty(),
			() -> "These contract methods were never successfully called on the " + recordingBackend.getType().getName() + " backend:\n" +
				String.join("\n", recordingBackend.getUncoveredContractMethods()));
	}
}
