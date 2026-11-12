#include "ULConfig.h"
#include "utils/JNIUtils.h"

#include <Ultralight/CAPI/CAPI_Config.h>

#include <Ultralight/platform/Config.h>

namespace
{
    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
    * Method:    nulCreateConfig
    * Signature: ()J
    */
    jlong JNICALL ULCreateConfig_Native(JNIEnv*, jclass)
    {
        return reinterpret_cast<jlong>(ulCreateConfig());
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
     * Method:    nulDestroyConfig
     * Signature: (J)V
     */
    void JNICALL ULDestroyConfig_Native(JNIEnv*, jclass, jlong ConfigHandle)
    {
        ulDestroyConfig(reinterpret_cast<ULConfig>(ConfigHandle));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
     * Method:    nulConfigSetCachePath
     * Signature: (JJ)V
     */
    void JNICALL ULConfigSetCachePath_Native(JNIEnv*, jclass, jlong ConfigHandle, jlong CachePathStringHandle)
    {
        const auto Config = reinterpret_cast<ULConfig>(ConfigHandle);
        const auto CachePathString = reinterpret_cast<ULString>(CachePathStringHandle);

        ulConfigSetCachePath(Config, CachePathString);
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
     * Method:    nulConfigSetResourcePathPrefix
     * Signature: (JJ)V
     */
    void JNICALL ULConfigSetResourcePathPrefix_Native(JNIEnv*, jclass, jlong ConfigHandle, jlong ResourcePathPrefixStringHandle)
    {
        const auto Config = reinterpret_cast<ULConfig>(ConfigHandle);
        const auto ResourcePathPrefixString = reinterpret_cast<ULString>(ResourcePathPrefixStringHandle);

        ulConfigSetResourcePathPrefix(Config, ResourcePathPrefixString);
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
     * Method:    nulConfigSetBitmapAlignment
     * Signature: (JI)V
     */
    void JNICALL ULConfigSetBitmapAlignment_Native(JNIEnv*, jclass, jlong ConfigHandle, jint BitmapRowAlignment)
    {
        ulConfigSetBitmapAlignment(reinterpret_cast<ULConfig>(ConfigHandle), BitmapRowAlignment);
    }

    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
    * Method:    nulConfigSetEffectQuality
    * Signature: (JI)V
    */
    void JNICALL ULConfigSetEffectQuality_Native(JNIEnv*, jclass, jlong ConfigHandle, jint EffectQualityNativeValue)
    {
        const auto Config = reinterpret_cast<ULConfig>(ConfigHandle);
        const auto EffectQuality = static_cast<ULEffectQuality>(EffectQualityNativeValue);

        ulConfigSetEffectQuality(Config, EffectQuality);
    }

    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
    * Method:    nulConfigSetUserStylesheet
    * Signature: (JJ)V
    */
    void JNICALL ULConfigSetUserStylesheet_Native(JNIEnv*, jclass, jlong ConfigHandle, jlong UserStylesheetStringHandle)
    {
        const auto Config = reinterpret_cast<ULConfig>(ConfigHandle);
        const auto UserStylesheetString = reinterpret_cast<ULString>(UserStylesheetStringHandle);

        ulConfigSetUserStylesheet(Config, UserStylesheetString);
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULConfigNatives_NativeMethods
     * Method:    nulConfigSetDeveloperMode
     * Signature: (JZ)V
     */
    void JNICALL ULConfigSetDeveloperMode_Native(JNIEnv*, jclass, jlong ConfigHandle, jboolean bEnableDeveloperMode)
    {
        const auto Config = reinterpret_cast<ULConfig>(ConfigHandle);

        ulConfigSetDeveloperMode(Config, static_cast<bool>(bEnableDeveloperMode));
    }
}

const char* CULConfig::GetClassName() const
{
    return "me/ayydxn/orbit/natives/jni/ultralight/JNIULConfigNatives$NativeMethods";
}

const std::vector<JNINativeMethod> CULConfig::GetNativeMethods() const
{
    return
    {
        CJNIUtils::MakeNativeMethod("nulCreateConfig", "()J", ULCreateConfig_Native),
        CJNIUtils::MakeNativeMethod("nulDestroyConfig", "(J)V", ULDestroyConfig_Native),
        CJNIUtils::MakeNativeMethod("nulConfigSetCachePath", "(JJ)V", ULConfigSetCachePath_Native),
        CJNIUtils::MakeNativeMethod("nulConfigSetResourcePathPrefix", "(JJ)V", ULConfigSetResourcePathPrefix_Native),
        CJNIUtils::MakeNativeMethod("nulConfigSetBitmapAlignment", "(JI)V", ULConfigSetBitmapAlignment_Native),
        CJNIUtils::MakeNativeMethod("nulConfigSetEffectQuality", "(JI)V", ULConfigSetEffectQuality_Native),
        CJNIUtils::MakeNativeMethod("nulConfigSetUserStylesheet", "(JJ)V", ULConfigSetUserStylesheet_Native),
        CJNIUtils::MakeNativeMethod("nulConfigSetDeveloperMode", "(JZ)V", ULConfigSetDeveloperMode_Native)
    };
}
