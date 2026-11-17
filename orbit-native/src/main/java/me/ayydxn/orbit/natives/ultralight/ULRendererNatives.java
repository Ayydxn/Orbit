package me.ayydxn.orbit.natives.ultralight;

public interface ULRendererNatives
{
    long create(long configHandle);

    void destroy(long handle);

    void update(long handle);

    void refreshDisplay(long handle, int displayID);

    void render(long handle);

    String getMemoryUsage(long handle);
}
