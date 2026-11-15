package me.ayydxn.orbit.natives.utils;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface NativeEnum
{
    int getNativeValue();

    static <E extends Enum<E> & NativeEnum> E resolveFromNativeValue(Class<E> enumClass, int nativeValue, E defaultValue)
    {
        for (E enumConstant : enumClass.getEnumConstants())
        {
            if (enumConstant.getNativeValue() == nativeValue)
                return enumConstant;
        }

        return defaultValue;
    }
}
