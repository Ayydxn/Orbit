package me.ayydxn.orbit.natives.recording;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NativeCallRecorderTest
{
	private static final IOException EXPECTED_FAILURE = new IOException("expected");

	private final NativeCallRecorder recorder = new NativeCallRecorder();
	private final FakeNatives natives = this.recorder.wrap(FakeNatives.class, new FakeNativesImplementation());

	@Test
	void passesResultsThroughAndRecordsTheCall()
	{
		assertEquals(5, this.natives.add(2, 3));
		assertTrue(this.recorder.getInvokedMethods().contains("FakeNatives#add(int, int)"));
	}

	@Test
	void exceptionsFromTheTargetPropagateUnchangedAndStillCount()
	{
		IOException thrown = assertThrows(IOException.class, this.natives::alwaysFails);

		assertSame(EXPECTED_FAILURE, thrown);
		assertTrue(this.recorder.getInvokedMethods().contains("FakeNatives#alwaysFails()"));
	}

	@Test
	void unsupportedOperationsDoNotCountAsCovered()
	{
		assertThrows(UnsupportedOperationException.class, this.natives::unbound);

		assertFalse(this.recorder.getInvokedMethods().contains("FakeNatives#unbound()"));
	}

	@Test
	void linkageErrorsDoNotCountAsCovered()
	{
		assertThrows(UnsatisfiedLinkError.class, this.natives::notLinked);

		assertFalse(this.recorder.getInvokedMethods().contains("FakeNatives#notLinked()"));
	}

	@Test
	void overloadsAreTrackedSeparately()
	{
		this.natives.overloaded(1);

		assertTrue(this.recorder.getInvokedMethods().contains("FakeNatives#overloaded(int)"));
		assertFalse(this.recorder.getInvokedMethods().contains("FakeNatives#overloaded(long)"));
	}

	@Test
	void objectMethodsAreNotRecorded()
	{
		this.natives.hashCode();
		this.natives.toString();

		assertTrue(this.recorder.getInvokedMethods().isEmpty());
	}

	@Test
	void requiredMethodsAreEveryAbstractMethodOnEveryContractAccessor()
	{
		Set<String> requiredMethods = NativeCallRecorder.getRequiredMethods(FakeBackend.class);

		assertEquals(Set.of(
			"FakeNatives#add(int, int)",
			"FakeNatives#alwaysFails()",
			"FakeNatives#unbound()",
			"FakeNatives#notLinked()",
			"FakeNatives#overloaded(int)",
			"FakeNatives#overloaded(long)",
			"FakeNatives#neverCalled()"
		), requiredMethods);
	}

	@Test
	void uncoveredMethodsAreWhatWasNeverSuccessfullyCalled()
	{
		this.natives.add(1, 1);
		this.natives.overloaded(1);
		this.natives.overloaded(1L);

		assertThrows(IOException.class, this.natives::alwaysFails);
		assertThrows(UnsupportedOperationException.class, this.natives::unbound);

		assertEquals(Set.of("FakeNatives#unbound()", "FakeNatives#notLinked()", "FakeNatives#neverCalled()"), this.recorder.getUncoveredMethods(FakeBackend.class));
	}

	private static final class FakeNativesImplementation implements FakeNatives
	{
		@Override
		public int add(int left, int right)
		{
			return left + right;
		}

		@Override
		public void alwaysFails() throws IOException
		{
			throw EXPECTED_FAILURE;
		}

		@Override
		public void unbound()
		{
			throw new UnsupportedOperationException("Not bound yet");
		}

		@Override
		public void notLinked()
		{
			throw new UnsatisfiedLinkError("Not registered");
		}

		@Override
		public long overloaded(int value)
		{
			return value;
		}

		@Override
		public long overloaded(long value)
		{
			return value;
		}

		@Override
		public void neverCalled()
		{
		}
	}

	private interface FakeNatives
	{
		int add(int left, int right);

		void alwaysFails() throws IOException;

		void unbound();

		void notLinked();

		long overloaded(int value);

		long overloaded(long value);

		void neverCalled();
	}

	private interface FakeBackend
	{
		FakeNatives fake();

		String describe();

		int count();
	}
}
