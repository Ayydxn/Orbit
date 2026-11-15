package me.ayydxn.orbit.ultralight.platform;

import me.ayydxn.orbit.natives.utils.NativeEnum;

public interface ULLogger
{
    void logMessage(String message, Level logLevel);

    enum Level implements NativeEnum
    {
        FATAL(0),
        ERROR(1),
        WARNING(2),
        INFO(3),
        DEBUG(4);

        private final int nativeValue;

        Level(int nativeValue)
        {
            this.nativeValue = nativeValue;
        }

        public static Level createFromNativeValue(int nativeValue)
        {
            return NativeEnum.resolveFromNativeValue(Level.class, nativeValue, INFO);
        }

        @Override
        public int getNativeValue()
        {
            return this.nativeValue;
        }
    }
}
