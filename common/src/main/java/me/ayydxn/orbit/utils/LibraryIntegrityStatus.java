package me.ayydxn.orbit.utils;

public class LibraryIntegrityStatus
{
    private final boolean isValid;

    private FailureReason failureReason;

    public LibraryIntegrityStatus(boolean isValid, FailureReason failureReason)
    {
        this.isValid = isValid;
        this.failureReason = failureReason;
    }

    public boolean isValid()
    {
        return isValid;
    }

    public FailureReason getFailureReason()
    {
        return failureReason;
    }

    public void setFailureReason(FailureReason failureReason)
    {
        this.failureReason = failureReason;
    }

    public enum FailureReason
    {
        UNKNOWN,

        JNI_BRIDGE_INAUTHENTIC,
        ULTRALIGHT_LIBRARIES_INAUTHENTIC
    }
}
