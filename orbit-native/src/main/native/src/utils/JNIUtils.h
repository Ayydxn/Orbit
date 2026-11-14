#pragma once

#include <jni.h>

#include <string>

typedef struct C_String* ULString;

class CJNIUtils
{
public:
    template <typename Function>
    static JNINativeMethod MakeNativeMethod(const char* Name, const char* Signature, Function BridgeFunction)
    {
        JNINativeMethod NativeMethod;
        NativeMethod.name = const_cast<char*>(Name);
        NativeMethod.signature = const_cast<char*>(Signature);
        NativeMethod.fnPtr = reinterpret_cast<void*>(BridgeFunction);

        return NativeMethod;
    }

    static void ThrowJavaException(JNIEnv* Environment, const char* ExceptionClassName, const std::string& Message);

    // Converts Ultralight's UTF-8 string to a Java string. The result is a local reference, or nullptr if the string is null.
    static jstring ToJavaString(JNIEnv* Environment, ULString String);

    // Creates a new ULString from a Java string. The caller owns the result and has to ulDestroyString it, unless Ultralight takes ownership of it (e.g. when returning it from a callback).
    // A null Java string gives an empty ULString.
    static ULString ToULString(JNIEnv* Environment, jstring JavaString);
};
