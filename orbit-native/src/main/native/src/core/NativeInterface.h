#pragma once

#include <jni.h>

#include <vector>

class INativeInterface
{
public:
    virtual ~INativeInterface() = default;

    virtual const char* GetClassName() const = 0;
    virtual const std::vector<JNINativeMethod> GetNativeMethods() const = 0;
};
