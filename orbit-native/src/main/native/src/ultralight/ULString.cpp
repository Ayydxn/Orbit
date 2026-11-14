#include "ULString.h"
#include "utils/JNIUtils.h"

#include <Ultralight/CAPI/CAPI_String.h>

#include <codecvt>
#include <string>

namespace
{
    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULStringNatives_NativeMethods
    * Method:    nulCreateString
    * Signature: (Ljava/lang/String;)J
    */
    jlong JNICALL ULCreateString_Native(JNIEnv* Environment, jclass, jstring JavaString)
    {
        if (!JavaString)
            return 0;

        const jchar* Characters = Environment->GetStringChars(JavaString, nullptr);
        if (!Characters)
            return 0;

        const jsize Length = Environment->GetStringLength(JavaString);

        std::string UTF8String;
        try
        {
            // Convert UTF-16 (Java) to standard UTF-8 (Ultralight)
            std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> Convert;
            const auto* Begin = reinterpret_cast<const char16_t*>(Characters);

            UTF8String = Convert.to_bytes(Begin, Begin + Length);
        }
        catch (const std::exception&)
        {
            Environment->ReleaseStringChars(JavaString, Characters);
            return 0;
        }

        Environment->ReleaseStringChars(JavaString, Characters);

        return reinterpret_cast<jlong>(ulCreateString(UTF8String.c_str()));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULStringNatives_NativeMethods
     * Method:    nulDestroyString
     * Signature: (J)V
     */
    void JNICALL ULDestroyString_Native(JNIEnv*, jclass, jlong StringHandle)
    {
        ulDestroyString(reinterpret_cast<ULString>(StringHandle));
    }

    /*
    * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULStringNatives_NativeMethods
    * Method:    nulStringGetData
    * Signature: (J)Ljava/lang/String;
    */
    jstring JNICALL ULStringGetData_Native(JNIEnv* Environment, jclass, jlong StringHandle)
    {
        const auto String = reinterpret_cast<ULString>(StringHandle);

        return CJNIUtils::ToJavaString(Environment, String);
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULStringNatives_NativeMethods
     * Method:    nulStringGetLength
     * Signature: (J)J
     */
    jlong JNICALL ULStringGetLength_Native(JNIEnv*, jclass, jlong StringHandle)
    {
        const auto String = reinterpret_cast<ULString>(StringHandle);

        return static_cast<jlong>(ulStringGetLength(String));
    }

    /*
     * Class:     me_ayydxn_orbit_natives_jni_ultralight_JNIULStringNatives_NativeMethods
     * Method:    nulStringIsEmpty
     * Signature: (J)Z
     */
    jboolean JNICALL ULStringIsEmpty_Native(JNIEnv*, jclass, jlong StringHandle)
    {
        return ulStringIsEmpty(reinterpret_cast<ULString>(StringHandle));
    }
}

const char* CULString::GetClassName() const
{
    return "me/ayydxn/orbit/natives/jni/ultralight/JNIULStringNatives$NativeMethods";
}

const std::vector<JNINativeMethod> CULString::GetNativeMethods() const
{
    return
    {
        CJNIUtils::MakeNativeMethod("nulCreateString", "(Ljava/lang/String;)J", ULCreateString_Native),
        CJNIUtils::MakeNativeMethod("nulDestroyString", "(J)V", ULDestroyString_Native),
        CJNIUtils::MakeNativeMethod("nulStringGetData", "(J)Ljava/lang/String;", ULStringGetData_Native),
        CJNIUtils::MakeNativeMethod("nulStringGetLength", "(J)J", ULStringGetLength_Native),
        CJNIUtils::MakeNativeMethod("nulStringIsEmpty", "(J)Z", ULStringIsEmpty_Native)
    };
}
