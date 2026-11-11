#include "core/NativeInterfaceRegistry.h"
#include "ultralight/ULString.h"
#include "ultralight/Ultralight.h"

#include <jni.h>

#include <memory>

namespace
{
    std::unique_ptr<CNativeInterfaceRegistry> GNativeInterfaceRegistry = nullptr;
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* VirtualMachine, void*)
{
    JNIEnv* Environment = nullptr;
    if (VirtualMachine->GetEnv(reinterpret_cast<void**>(&Environment), JNI_VERSION_10) != JNI_OK)
        return JNI_ERR;

    GNativeInterfaceRegistry = std::make_unique<CNativeInterfaceRegistry>();
    GNativeInterfaceRegistry->Add(std::make_shared<CUltralight>());
    GNativeInterfaceRegistry->Add(std::make_shared<CULString>());

    GNativeInterfaceRegistry->RegisterInterfaces(Environment);

    return JNI_VERSION_10;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM* VirtualMachine, void*)
{
    JNIEnv* Environment = nullptr;
    if (VirtualMachine->GetEnv(reinterpret_cast<void**>(&Environment), JNI_VERSION_10) != JNI_OK)
        return;

    GNativeInterfaceRegistry.reset();
}
