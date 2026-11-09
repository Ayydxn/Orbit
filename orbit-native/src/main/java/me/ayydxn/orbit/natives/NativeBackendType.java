package me.ayydxn.orbit.natives;

import java.util.Optional;

public enum NativeBackendType
{
	JNI("JNI", "me.ayydxn.orbit.natives.jni.JNINativeBackend", true),
	FFM("FFM", "me.ayydxn.orbit.natives.ffm.FFMNativeBackend", false);

	private final String name;
	private final String backendClassName;
	private final boolean usesJNIBridge;

	NativeBackendType(String name, String backendClassName, boolean usesJNIBridge)
	{
		this.name = name;
		this.backendClassName = backendClassName;
		this.usesJNIBridge = usesJNIBridge;
	}

	public static Optional<NativeBackendType> fromName(String name)
	{
		for (NativeBackendType nativeBackendType : NativeBackendType.values())
		{
			if (nativeBackendType.name.equalsIgnoreCase(name))
				return Optional.of(nativeBackendType);
		}

		return Optional.empty();
	}

	public String getName()
	{
		return this.name;
	}

	// This is a class name rather than a Class reference so that the selector never has to link against a backend that isn't present (e.g. FFM on Java 17).
	public String getBackendClassName()
	{
		return this.backendClassName;
	}

	public boolean usesJNIBridge()
	{
		return this.usesJNIBridge;
	}
}
