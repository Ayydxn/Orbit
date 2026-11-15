package me.ayydxn.orbit.natives.ultralight;

import me.ayydxn.orbit.ultralight.platform.ULFileSystem;
import me.ayydxn.orbit.ultralight.platform.ULFontLoader;
import me.ayydxn.orbit.ultralight.platform.ULLogger;

public interface ULPlatformNatives
{
    void setLogger(ULLogger logger);

    void setFileSystem(ULFileSystem fileSystem);

    void setFontLoader(ULFontLoader fontLoader);

    void shutdown();
}
