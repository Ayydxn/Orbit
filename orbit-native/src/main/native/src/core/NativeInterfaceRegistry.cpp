#include "NativeInterfaceRegistry.h"

CNativeInterfaceRegistry::~CNativeInterfaceRegistry()
{
    // Clear the vector elements and free its internal capacity
    m_NativeInterfaces = std::vector<std::shared_ptr<INativeInterface>>();
}

void CNativeInterfaceRegistry::Add(const std::shared_ptr<INativeInterface>& NativeInterface)
{
    m_NativeInterfaces.push_back(NativeInterface);
}

void CNativeInterfaceRegistry::RegisterInterfaces(JNIEnv* Environment) const
{
    for (const std::shared_ptr<INativeInterface>& NativeInterface : m_NativeInterfaces)
    {
        const jclass Class = Environment->FindClass(NativeInterface->GetClassName());
        if (Class == nullptr)
        {
            Environment->ExceptionDescribe();
            Environment->ExceptionClear();

            continue;
        }

        const std::vector<JNINativeMethod>& NativeMethods = NativeInterface->GetNativeMethods();
        if (Environment->RegisterNatives(Class, NativeMethods.data(), static_cast<jint>(NativeMethods.size())) != JNI_OK)
        {
            if (Environment->ExceptionCheck())
            {
                Environment->ExceptionDescribe();
                Environment->ExceptionClear();
            }
        }

        Environment->DeleteLocalRef(Class);
    }
}
