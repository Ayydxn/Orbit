#pragma once

#include "core/NativeInterface.h"

class CULConfig : public INativeInterface
{
public:
    ~CULConfig() override = default;

    const char* GetClassName() const override;
    const std::vector<JNINativeMethod> GetNativeMethods() const override;
};
