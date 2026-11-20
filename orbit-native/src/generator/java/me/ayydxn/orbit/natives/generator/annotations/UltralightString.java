package me.ayydxn.orbit.natives.generator.annotations;

import java.lang.annotation.*;

/** A returned {@code String} that is a {@code ULString}. With owned = true the native side destroys it after converting. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UltralightString
{
    boolean owned() default false;
}
