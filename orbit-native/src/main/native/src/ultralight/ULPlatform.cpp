#include "ULPlatform.h"
#include "ultralight/adapters/FileSystemCallbackAdapter.h"
#include "ultralight/adapters/FontLoaderCallbackAdapter.h"
#include "ultralight/adapters/LoggerCallbackAdapter.h"
#include "utils/JNIUtils.h"

namespace
{
    void ReleaseCallbackAdapters()
    {
        CLoggerCallbackAdapter::Release();
        CFileSystemCallbackAdapter::Release();
        CFontLoaderCallbackAdapter::Release();
    }

    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULPlatformNatives_NativeMethods
    * Method:    nulPlatformSetLogger
    * Signature: (Lme/ayydxn/orbit/ultralight/platform/ULLogger;)V
    */
    void JNICALL ULPlatformSetLogger_Native(JNIEnv* Environment, jclass, jobject JavaLoggerInstance)
    {
        CLoggerCallbackAdapter::Install(Environment, JavaLoggerInstance);
    }

    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULPlatformNatives_NativeMethods
    * Method:    nulPlatformSetFileSystem
    * Signature: (Lme/ayydxn/orbit/ultralight/platform/ULFileSystem;)V
    */
    void JNICALL ULPlatformSetFileSystem_Native(JNIEnv* Environment, jclass, jobject JavaFileSystemInstance)
    {
        CFileSystemCallbackAdapter::Install(Environment, JavaFileSystemInstance);
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULPlatformNatives_NativeMethods
     * Method:    nulPlatformSetFontLoader
     * Signature: (Lme/ayydxn/orbit/ultralight/platform/ULFontLoader;)V
     */
    void JNICALL ULPlatformSetFontLoader_Native(JNIEnv* Environment, jclass, jobject JavaFontLoaderInstance)
    {
        CFontLoaderCallbackAdapter::Install(Environment, JavaFontLoaderInstance);
    }

    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULPlatformNatives_NativeMethods
    * Method:    nulPlatformShutdown
    * Signature: ()V
    */
    void JNICALL ULPlatformShutdown_Native(JNIEnv*, jclass)
    {
        ReleaseCallbackAdapters();
    }
}

CULPlatform::~CULPlatform()
{
    ReleaseCallbackAdapters();
}

const char* CULPlatform::GetClassName() const
{
    return "me/ayydxn/orbit/natives/jni/ultralight/JNIULPlatformNatives$NativeMethods";
}

const std::vector<JNINativeMethod> CULPlatform::GetNativeMethods() const
{
    return
    {
        CJNIUtils::MakeNativeMethod("nulPlatformSetLogger", "(Lme/ayydxn/orbit/ultralight/platform/ULLogger;)V", ULPlatformSetLogger_Native),
        CJNIUtils::MakeNativeMethod("nulPlatformSetFileSystem", "(Lme/ayydxn/orbit/ultralight/platform/ULFileSystem;)V", ULPlatformSetFileSystem_Native),
        CJNIUtils::MakeNativeMethod("nulPlatformSetFontLoader", "(Lme/ayydxn/orbit/ultralight/platform/ULFontLoader;)V", ULPlatformSetFontLoader_Native),
        CJNIUtils::MakeNativeMethod("nulPlatformShutdown", "()V", ULPlatformShutdown_Native)
    };
}
