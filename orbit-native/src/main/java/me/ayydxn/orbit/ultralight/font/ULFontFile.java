package me.ayydxn.orbit.ultralight.font;

public final class ULFontFile
{
    private final Source source;
	private final String filePath;
	private final byte[] buffer;

	private ULFontFile(Source source, String filePath, byte[] buffer)
	{
		this.source = source;
		this.filePath = filePath;
		this.buffer = buffer;
	}

	public static ULFontFile fromFilePath(String filePath)
	{
		if (filePath == null)
			throw new IllegalArgumentException("Cannot create a ULFontFile from a null file path!");

		return new ULFontFile(Source.FILE_PATH, filePath, null);
	}

	public static ULFontFile fromBuffer(byte[] buffer)
	{
		if (buffer == null)
			throw new IllegalArgumentException("Cannot create a ULFontFile from a null buffer!");

		return new ULFontFile(Source.BUFFER, null, buffer);
	}

	public Source getSource()
	{
		return this.source;
	}

	public String getFilePath()
	{
		return this.filePath;
	}

	public byte[] getBuffer()
	{
		return this.buffer;
	}

    public enum Source
    {
        FILE_PATH,
        BUFFER
    }
}
