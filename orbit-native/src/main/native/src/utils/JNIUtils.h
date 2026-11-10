#pragma once

#include <jni.h>

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
};
