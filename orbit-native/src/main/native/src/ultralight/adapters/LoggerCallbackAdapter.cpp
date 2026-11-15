#include "LoggerCallbackAdapter.h"
#include "core/jni/JNICallScope.h"

#include <Ultralight/CAPI/CAPI_Platform.h>

void CLoggerCallbackAdapter::Resolve(CJNIResolver& Resolver)
{
    const TGlobalRef<jclass> LoggerClass = Resolver.Class("me/ayydxn/orbit/ultralight/platform/ULLogger");
    m_LogMessageMethodID = Resolver.Method(LoggerClass, "logMessage", "(Ljava/lang/String;Lme/ayydxn/orbit/ultralight/platform/ULLogger$Level;)V");

    m_LevelClass = Resolver.Class("me/ayydxn/orbit/ultralight/platform/ULLogger$Level");
    m_LevelFromNativeValueMethodID = Resolver.StaticMethod(m_LevelClass, "createFromNativeValue", "(I)Lme/ayydxn/orbit/ultralight/platform/ULLogger$Level;");
}

void CLoggerCallbackAdapter::Apply()
{
    ULLogger Logger;
    Logger.log_message = &CLoggerCallbackAdapter::LogMessage_Trampoline;

    ulPlatformSetLogger(Logger);
}

void CLoggerCallbackAdapter::LogMessage_Trampoline(ULLogLevel LogLevel, ULString Message)
{
    const std::shared_ptr<CLoggerCallbackAdapter> Self = Active();
    if (!Self)
        return;

    const CJNICallScope CallScope;
    if (!CallScope)
        return;

    const jobject LevelObject = CallScope->CallStaticObjectMethod(Self->m_LevelClass, Self->m_LevelFromNativeValueMethodID, static_cast<jint>(LogLevel));
    if (CallScope.CheckException())
        return;

    const jstring MessageString = CJNIUtils::ToJavaString(CallScope.Env(), Message);

    CallScope->CallVoidMethod(Self->GetJavaInstance(), Self->m_LogMessageMethodID, MessageString, LevelObject);
}
