package me.ayydxn.orbit.ultralight.platform;

public interface ULFileSystem
{
    boolean fileExists(String path);

    String getFileMimeType(String path);

    String getFileCharset(String path);

    byte[] openFile(String path);
}
