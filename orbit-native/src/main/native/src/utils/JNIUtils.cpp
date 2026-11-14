#include "JNIUtils.h"

#include <Ultralight/CAPI/CAPI_String.h>

#include <codecvt>
#include <locale>

void CJNIUtils::ThrowJavaException(JNIEnv* Environment, const char* ExceptionClassName, const std::string& Message)
{
    const jclass ExceptionClass = Environment->FindClass(ExceptionClassName);
    if (!ExceptionClass)
        return; // FindClass has already left a NoClassDefFoundError pending.

    Environment->ThrowNew(ExceptionClass, Message.c_str());
    Environment->DeleteLocalRef(ExceptionClass);
}

jstring CJNIUtils::ToJavaString(JNIEnv* Environment, ULString String)
{
    if (!String)
        return nullptr;

    const char* Data = ulStringGetData(String);
    if (!Data || Data[0] == '\0')
        return Environment->NewString(nullptr, 0);

    try
    {
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> Convert;
        const std::u16string UTF16String = Convert.from_bytes(Data);

        return Environment->NewString(reinterpret_cast<const jchar*>(UTF16String.data()), static_cast<jsize>(UTF16String.length()));
    }
    catch (const std::exception&)
    {
        return Environment->NewStringUTF(Data);
    }
}

ULString CJNIUtils::ToULString(JNIEnv* Environment, jstring JavaString)
{
    if (!JavaString)
        return ulCreateString("");

    const jchar* Characters = Environment->GetStringChars(JavaString, nullptr);
    if (!Characters)
        return ulCreateString("");

    const jsize Length = Environment->GetStringLength(JavaString);

    std::string UTF8String;

    try
    {
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> Convert;
        const char16_t* Begin = reinterpret_cast<const char16_t*>(Characters);

        UTF8String = Convert.to_bytes(Begin, Begin + Length);
    }
    catch (const std::exception&)
    {
        // Leave it empty rather than hand Ultralight a half-converted string.
        UTF8String.clear();
    }

    Environment->ReleaseStringChars(JavaString, Characters);

    return ulCreateString(UTF8String.c_str());
}
