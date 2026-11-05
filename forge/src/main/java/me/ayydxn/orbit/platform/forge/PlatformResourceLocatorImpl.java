package me.ayydxn.orbit.platform.forge;

import java.io.InputStream;

public class PlatformResourceLocatorImpl
{
    public static InputStream getResourceAsStream(String path)
    {
        return PlatformResourceLocatorImpl.class.getClassLoader().getResourceAsStream(path);
    }
}
