#pragma once

#include "core/NativeInterface.h"

class CULPlatform : public INativeInterface
{
public:
    ~CULPlatform() override;

    const char* GetClassName() const override;
    const std::vector<JNINativeMethod> GetNativeMethods() const override;
};
