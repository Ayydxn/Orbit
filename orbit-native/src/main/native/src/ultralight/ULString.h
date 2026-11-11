#pragma once

#include "core/NativeInterface.h"

class CULString : public INativeInterface
{
public:
    ~CULString() override = default;

    const char* GetClassName() const override;
    const std::vector<JNINativeMethod> GetNativeMethods() const override;
};
