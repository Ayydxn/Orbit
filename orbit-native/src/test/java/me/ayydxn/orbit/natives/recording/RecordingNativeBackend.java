package me.ayydxn.orbit.natives.recording;

import me.ayydxn.orbit.natives.NativeBackendType;
import me.ayydxn.orbit.natives.OrbitNativeBackend;
import me.ayydxn.orbit.natives.exceptions.NativeBackendException;
import me.ayydxn.orbit.natives.ultralight.UltralightNatives;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A backend that passes every call through to a real one while recording which native methods were used.
 * <p>
 * Adding an accessor to {@link OrbitNativeBackend} won't compile here until it's wrapped, which is what keeps new contracts inside the coverage check.
 */
public final class RecordingNativeBackend implements OrbitNativeBackend
{
	private final OrbitNativeBackend delegate;
	private final NativeCallRecorder recorder = new NativeCallRecorder();

	private final UltralightNatives ultralightNatives;

	public RecordingNativeBackend(OrbitNativeBackend delegate)
	{
		this.delegate = delegate;

		this.ultralightNatives = this.recorder.wrap(UltralightNatives.class, delegate.ultralight());
	}

	@Override
	public void initialize() throws NativeBackendException
	{
		this.delegate.initialize();
	}

	@Override
	public @NotNull NativeBackendType getType()
	{
		return this.delegate.getType();
	}

	@Override
	public @NotNull UltralightNatives ultralight()
	{
		return this.ultralightNatives;
	}

	public Set<String> getUncoveredContractMethods()
	{
		return this.recorder.getUncoveredMethods(OrbitNativeBackend.class);
	}
}
