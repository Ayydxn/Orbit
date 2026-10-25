#include <jni.h>

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* VirtualMachine, void*)
{
    JNIEnv* Environment = nullptr;
    if (VirtualMachine->GetEnv(reinterpret_cast<void**>(&Environment), JNI_VERSION_10) != JNI_OK)
        return JNI_ERR;

    return JNI_VERSION_10;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM* VirtualMachine, void*)
{
    JNIEnv* Environment = nullptr;
    if (VirtualMachine->GetEnv(reinterpret_cast<void**>(&Environment), JNI_VERSION_10) != JNI_OK)
        return;
}
