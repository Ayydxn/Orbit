package me.ayydxn.orbit.natives.generator.annotations;

import java.lang.annotation.*;

/** A returned {@code String} that is a {@code const char*} owned by the library. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NativeString
{
}
