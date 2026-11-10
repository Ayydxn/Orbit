#pragma once

#include "core/NativeInterface.h"

class CUltralight : public INativeInterface
{
public:
    ~CUltralight() override = default;

    const char* GetClassName() const override;
    const std::vector<JNINativeMethod> GetNativeMethods() const override;
};
