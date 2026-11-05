package me.ayydxn.orbit.platform.fabric;

import java.io.InputStream;

public class PlatformResourceLocatorImpl
{
    public static InputStream getResourceAsStream(String path)
    {
        return PlatformResourceLocatorImpl.class.getResourceAsStream(path);
    }
}
