package me.ayydxn.orbit.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.apache.commons.lang3.NotImplementedException;

import java.io.InputStream;

public class PlatformResourceLocator
{
    @ExpectPlatform
    public static InputStream getResourceAsStream(String path)
    {
        throw new NotImplementedException("Not implemented on this modding platform!");
    }
}
