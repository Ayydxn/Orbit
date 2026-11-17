package me.ayydxn.orbit.ultralight;

import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.natives.ultralight.ULRendererNatives;

public final class ULRenderer
{
    private static final ULRendererNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulRenderer();

    private long handle;

    public ULRenderer(ULConfig config)
    {
        this.handle = NATIVES.create(config.getHandle());
    }

    public void destroy()
    {
        if (this.handle != 0)
        {
            NATIVES.destroy(this.handle);

            this.handle = 0;
        }
    }

    public void update()
    {
        NATIVES.update(this.handle);
    }

    public void refreshDisplay(int displayID)
    {
        NATIVES.refreshDisplay(this.handle, displayID);
    }

    public void render()
    {
        NATIVES.render(this.handle);
    }

    public String getMemoryUsage()
    {
        return NATIVES.getMemoryUsage(this.handle);
    }
}
