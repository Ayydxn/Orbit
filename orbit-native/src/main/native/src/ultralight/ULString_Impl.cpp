#include "utils/JNIUtils.h"

#include <Ultralight/CAPI/CAPI_String.h>

#include <jni.h>

#include <codecvt>
#include <locale>
#include <string>

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
 * Method:    nulStringGetData
 * Signature: (J)Ljava/lang/String;
 */
jstring JNICALL ULStringGetData_Native(JNIEnv* Environment, jclass, jlong Handle)
{
    return CJNIUtils::ToJavaString(Environment, reinterpret_cast<ULString>(Handle));
}
