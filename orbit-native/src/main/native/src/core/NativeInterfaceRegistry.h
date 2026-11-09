#pragma once

#include "NativeInterface.h"

#include <memory>

class CNativeInterfaceRegistry
{
public:
    CNativeInterfaceRegistry() = default;
    ~CNativeInterfaceRegistry();

    void Add(const std::shared_ptr<INativeInterface>& NativeInterface);
    void RegisterInterfaces(JNIEnv* Environment) const;
private:
    std::vector<std::shared_ptr<INativeInterface>> m_NativeInterfaces;
};
