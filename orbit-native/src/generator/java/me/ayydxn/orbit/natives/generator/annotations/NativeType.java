package me.ayydxn.orbit.natives.generator.annotations;

import java.lang.annotation.*;

/** The C type a numeric Java parameter is cast to (e.g. {@code unsigned int}, {@code ULEffectQuality}). */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface NativeType
{
    String value();
}
