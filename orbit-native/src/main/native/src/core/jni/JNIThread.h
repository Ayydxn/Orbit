#pragma once

#include <jni.h>

class CJNIThread
{
public:
    static void Initialize(JavaVM* VirtualMachine);
    static void Shutdown();

    // Returns the JNIEnv of the calling thread. Threads that the JVM doesn't know about (e.g. one owned by Ultralight) are attached as daemon threads once,
    // and stay attached until the thread exits. Returns nullptr if the JVM isn't available.
    static JNIEnv* GetEnvironment();

    // Prints and clears any pending Java exception. Returns true if there was one.
    static bool ReportException(JNIEnv* Environment);
};
