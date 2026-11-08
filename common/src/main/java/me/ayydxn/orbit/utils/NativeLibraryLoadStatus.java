package me.ayydxn.orbit.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record NativeLibraryLoadStatus(@NotNull State state, FailureReason failureReason)
{
    public NativeLibraryLoadStatus
    {
        Objects.requireNonNull(state);

        if (state == State.FAILED && failureReason == null)
            throw new IllegalArgumentException("A failed native load status must have a failure reason!");
    }

    public static NativeLibraryLoadStatus notLoaded()
    {
        return new NativeLibraryLoadStatus(State.NOT_LOADED, null);
    }

	public static NativeLibraryLoadStatus loading()
	{
		return new NativeLibraryLoadStatus(State.LOADING, null);
	}

    public static NativeLibraryLoadStatus loaded()
    {
        return new NativeLibraryLoadStatus(State.LOADED, null);
    }

    // Loaded, but without verifying the JNI bridge against the release checksum (development environment or the check was disabled).
    public static NativeLibraryLoadStatus loadedUnverified()
    {
        return new NativeLibraryLoadStatus(State.LOADED_UNVERIFIED, null);
    }

    public static NativeLibraryLoadStatus failed(FailureReason failureReason)
    {
        return new NativeLibraryLoadStatus(State.FAILED, Objects.requireNonNull(failureReason));
    }

    public boolean isFailed()
    {
        return this.state == State.FAILED;
    }

    public enum State
    {
        NOT_LOADED,
		LOADING,
        LOADED,
        LOADED_UNVERIFIED,
        FAILED
    }

    public enum FailureReason
    {
        UNSUPPORTED_PLATFORM,
        ULTRALIGHT_DOWNLOAD_FAILED,
        ULTRALIGHT_LIBRARIES_INAUTHENTIC,
        JNI_BRIDGE_MISSING,
        JNI_BRIDGE_INAUTHENTIC,
        INTEGRITY_CHECK_UNAVAILABLE,
        NATIVE_LOAD_FAILED,
        UNKNOWN
    }
}
