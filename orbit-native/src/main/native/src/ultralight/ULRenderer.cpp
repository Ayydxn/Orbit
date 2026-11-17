#include "ULRenderer.h"
#include "utils/JNIUtils.h"

#include <Ultralight/CAPI/CAPI_Renderer.h>
#include <Ultralight/CAPI/CAPI_String.h>

namespace
{
    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULRendererNatives_NativeMethods
    * Method:    nulCreateRenderer
    * Signature: (J)J
    */
    jlong JNICALL ULCreateRenderer_Native(JNIEnv*, jclass, jlong ConfigHandle)
    {
        const auto Config = reinterpret_cast<ULConfig>(ConfigHandle);

        return reinterpret_cast<jlong>(ulCreateRenderer(Config));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULRendererNatives_NativeMethods
     * Method:    nulDestroyRenderer
     * Signature: (J)V
     */
    void JNICALL ULDestroyRenderer_Native(JNIEnv*, jclass, jlong RendererHandle)
    {
        ulDestroyRenderer(reinterpret_cast<ULRenderer>(RendererHandle));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULRendererNatives_NativeMethods
     * Method:    nulUpdate
     * Signature: (J)V
     */
    void JNICALL ULUpdate_Native(JNIEnv*, jclass, jlong RendererHandle)
    {
        ulUpdate(reinterpret_cast<ULRenderer>(RendererHandle));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULRendererNatives_NativeMethods
     * Method:    nulRefreshDisplay
     * Signature: (JI)V
     */
    void JNICALL ULRefreshDisplay_Native(JNIEnv*, jclass, jlong RendererHandle, jint DisplayID)
    {
        const auto Renderer = reinterpret_cast<ULRenderer>(RendererHandle);

        ulRefreshDisplay(Renderer, static_cast<unsigned int>(DisplayID));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULRendererNatives_NativeMethods
     * Method:    nulRender
     * Signature: (J)V
     */
    void JNICALL ULRender_Native(JNIEnv*, jclass, jlong RendererHandle)
    {
        ulRender(reinterpret_cast<ULRenderer>(RendererHandle));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULRendererNatives_NativeMethods
     * Method:    nulGetMemoryUsage
     * Signature: (J)Ljava/lang/String;
     */
    jstring JNICALL ULGetMemoryUsage_Native(JNIEnv* Environment, jclass, jlong RendererHandle)
    {
        const auto Renderer = reinterpret_cast<ULRenderer>(RendererHandle);
        const auto MemoryUsageULString = ulGetMemoryUsage(Renderer);

        if (!MemoryUsageULString)
            return nullptr;

        const auto MemoryUsageString = ulStringGetData(MemoryUsageULString);
        const jstring result = Environment->NewStringUTF(MemoryUsageString);

        ulDestroyString(MemoryUsageULString);

        return result;
    }
}

const char* CULRenderer::GetClassName() const
{
    return "me/ayydxn/orbit/natives/jni/ultralight/JNIULRendererNatives$NativeMethods";
}

const std::vector<JNINativeMethod> CULRenderer::GetNativeMethods() const
{
    return
    {
        CJNIUtils::MakeNativeMethod("nulCreateRenderer", "(J)J", ULCreateRenderer_Native),
        CJNIUtils::MakeNativeMethod("nulDestroyRenderer", "(J)V", ULDestroyRenderer_Native),
        CJNIUtils::MakeNativeMethod("nulUpdate", "(J)V", ULUpdate_Native),
        CJNIUtils::MakeNativeMethod("nulRefreshDisplay", "(JI)V", ULRefreshDisplay_Native),
        CJNIUtils::MakeNativeMethod("nulRender", "(J)V", ULRender_Native),
        CJNIUtils::MakeNativeMethod("nulGetMemoryUsage", "(J)Ljava/lang/String;", ULGetMemoryUsage_Native)
    };
}
