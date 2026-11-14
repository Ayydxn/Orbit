#include "JNIThread.h"

#include <atomic>

namespace
{
    std::atomic<JavaVM*> GVirtualMachine = nullptr;

    // Only ever touched on a thread that we attached ourselves. Its destructor runs when that thread exits, which detaches it again.
    struct FThreadAttachment
    {
        bool bAttachedByUs = false;

        ~FThreadAttachment()
        {
            if (!bAttachedByUs)
                return;

            if (JavaVM* VirtualMachine = GVirtualMachine.load())
                VirtualMachine->DetachCurrentThread();
        }
    };

    thread_local FThreadAttachment GThreadAttachment;
}

void CJNIThread::Initialize(JavaVM* VirtualMachine)
{
    GVirtualMachine.store(VirtualMachine);
}

void CJNIThread::Shutdown()
{
    GVirtualMachine.store(nullptr);
}

JNIEnv* CJNIThread::GetEnvironment()
{
    JavaVM* VirtualMachine = GVirtualMachine.load();
    if (!VirtualMachine)
        return nullptr;

    JNIEnv* Environment = nullptr;

    const jint Result = VirtualMachine->GetEnv(reinterpret_cast<void**>(&Environment), JNI_VERSION_10);
    if (Result == JNI_OK)
        return Environment;

    if (Result != JNI_EDETACHED)
        return nullptr;

    if (VirtualMachine->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&Environment), nullptr) != JNI_OK)
        return nullptr;

    GThreadAttachment.bAttachedByUs = true;

    return Environment;
}

bool CJNIThread::ReportException(JNIEnv* Environment)
{
    if (!Environment || !Environment->ExceptionCheck())
        return false;

    Environment->ExceptionDescribe();
    Environment->ExceptionClear();

    return true;
}
