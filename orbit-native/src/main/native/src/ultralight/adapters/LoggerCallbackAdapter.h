#pragma once

#include "core/CallbackAdapter.h"

#include <Ultralight/CAPI/CAPI_Logger.h>

class CLoggerCallbackAdapter final : public TPlatformCallbackAdapter<CLoggerCallbackAdapter>
{
public:
    CLoggerCallbackAdapter(JNIEnv* Environment, jobject JavaInstance)
        : TPlatformCallbackAdapter(Environment, JavaInstance) {}
public:
    friend class TPlatformCallbackAdapter<CLoggerCallbackAdapter>;
private:
    void Resolve(CJNIResolver& Resolver);
    void Apply();

    static void LogMessage_Trampoline(ULLogLevel LogLevel, ULString Message);
private:
    TGlobalRef<jclass> m_LevelClass;

    jmethodID m_LogMessageMethodID = nullptr;
    jmethodID m_LevelFromNativeValueMethodID = nullptr;
};
