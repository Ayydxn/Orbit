package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.ultralight.ULStringNatives;
import org.jetbrains.annotations.ApiStatus;

public final class ULString implements AutoCloseable
{
	private static final ULStringNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulString();

	private long handle;

	private ULString(long handle)
	{
		this.handle = handle;
	}

	public static ULString create(String string)
	{
		if (string == null)
			throw new IllegalArgumentException("Cannot create a ULString with a null Java string!");

		return new ULString(NATIVES.createString(string));
	}

	@Override
	public void close()
	{
		if (this.handle != 0L)
		{
			NATIVES.destroyString(this.handle);

			this.handle = 0L;
		}
	}

	public String getData()
	{
		return NATIVES.getData(this.handle);
	}

	public long getLength()
	{
		return NATIVES.getLength(this.handle);
	}

	public boolean isEmpty()
	{
		return NATIVES.isEmpty(this.handle);
	}

	@ApiStatus.Internal
	public long getHandle()
	{
		return this.handle;
	}
}
