package me.ayydxn.orbit.natives.generator.annotations;

import java.lang.annotation.*;

/** A {@code long} that carries a native handle of the given C type (e.g. {@code ULConfig}). */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface NativePointer
{
    String value();
}
