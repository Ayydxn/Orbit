#include "Ultralight.h"
#include "utils/JNIUtils.h"

#include <Ultralight/CAPI/CAPI_Defines.h>

namespace
{
    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIUltralightNatives_NativeMethods
    * Method:    nulVersionString
    * Signature: ()Ljava/lang/String;
    */
    jstring JNICALL ULVersionString_Native(JNIEnv* Environment, jclass)
    {
        return Environment->NewStringUTF(ulVersionString());
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIUltralightNatives_NativeMethods
     * Method:    nulWebKitVersionString
     * Signature: ()Ljava/lang/String;
     */
    jstring JNICALL ULWebKitVersionString_Native(JNIEnv* Environment, jclass)
    {
        return Environment->NewStringUTF(ulWebKitVersionString());
    }
}

const char* CUltralight::GetClassName() const
{
    return "me/ayydxn/orbit/natives/jni/ultralight/JNIUltralightNatives$NativeMethods";
}

const std::vector<JNINativeMethod> CUltralight::GetNativeMethods() const
{
    return
    {
        CJNIUtils::MakeNativeMethod("nulVersionString", "()Ljava/lang/String;", ULVersionString_Native),
        CJNIUtils::MakeNativeMethod("nulWebKitVersionString", "()Ljava/lang/String;", ULWebKitVersionString_Native)
    };
}
