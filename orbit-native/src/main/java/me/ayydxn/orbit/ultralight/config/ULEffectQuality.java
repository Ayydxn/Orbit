package me.ayydxn.orbit.ultralight.config;

import org.jetbrains.annotations.ApiStatus;

public enum ULEffectQuality
{
    LOW(0),
    MEDIUM(1),
    HIGHEST(2);

    private final int nativeValue;

    ULEffectQuality(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @ApiStatus.Internal
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
