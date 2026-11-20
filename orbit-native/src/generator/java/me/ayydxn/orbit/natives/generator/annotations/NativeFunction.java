package me.ayydxn.orbit.natives.generator.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NativeFunction
{
    /** The C function this method calls. */
    String value();

    /** The Java side and the registration are generated, but the C++ body is written by hand. */
    boolean custom() default false;
}
