package me.ayydxn.orbit.natives.exceptions;

public final class NativeBackendException extends Exception
{
	public NativeBackendException(String message)
	{
		super(message);
	}

	public NativeBackendException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
