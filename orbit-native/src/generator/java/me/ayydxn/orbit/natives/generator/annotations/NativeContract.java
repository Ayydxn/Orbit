package me.ayydxn.orbit.natives.generator.annotations;

import java.lang.annotation.*;

/**
 * Marks an interface as a set of native functions. Everything else (class names, files, registration) is derived from it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NativeContract
{
    /**
     * C/C++ headers the generated code includes, relative to the include path.
     */
    String[] headers();
}
