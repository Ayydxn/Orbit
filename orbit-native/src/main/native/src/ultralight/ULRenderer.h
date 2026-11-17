#pragma once

#include "core/NativeInterface.h"

class CULRenderer : public INativeInterface
{
public:
    ~CULRenderer() override = default;

    const char* GetClassName() const override;
    const std::vector<JNINativeMethod> GetNativeMethods() const override;
};
