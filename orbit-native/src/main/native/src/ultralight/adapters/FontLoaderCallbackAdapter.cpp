#include "FontLoaderCallbackAdapter.h"
#include "core/jni/JNICallScope.h"

#include <Ultralight/CAPI/CAPI_Buffer.h>
#include <Ultralight/CAPI/CAPI_Platform.h>

void CFontLoaderCallbackAdapter::Resolve(CJNIResolver& Resolver)
{
    const TGlobalRef<jclass> FontLoaderClass = Resolver.Class("me/ayydxn/orbit/ultralight/platform/ULFontLoader");
    const TGlobalRef<jclass> FontFileClass = Resolver.Class("me/ayydxn/orbit/ultralight/font/ULFontFile");
    const TGlobalRef<jclass> SourceClass = Resolver.Class("me/ayydxn/orbit/ultralight/font/ULFontFile$Source");

    m_GetFallbackFontMethodID = Resolver.Method(FontLoaderClass, "getFallbackFont", "()Ljava/lang/String;");
    m_GetFallbackFontForCharactersMethodID = Resolver.Method(FontLoaderClass, "getFallbackFontForCharacters", "(Ljava/lang/String;IZ)Ljava/lang/String;");
    m_LoadMethodID = Resolver.Method(FontLoaderClass, "load", "(Ljava/lang/String;IZ)Lme/ayydxn/orbit/ultralight/font/ULFontFile;");

    m_FontFileGetSourceMethodID = Resolver.Method(FontFileClass, "getSource", "()Lme/ayydxn/orbit/ultralight/font/ULFontFile$Source;");
    m_FontFileGetFilePathMethodID = Resolver.Method(FontFileClass, "getFilePath", "()Ljava/lang/String;");
    m_FontFileGetBufferMethodID = Resolver.Method(FontFileClass, "getBuffer", "()[B");

    m_SourceFilePath = Resolver.StaticObject(SourceClass, "FILE_PATH", "Lme/ayydxn/orbit/ultralight/font/ULFontFile$Source;");
}

void CFontLoaderCallbackAdapter::Apply()
{
    ULFontLoader FontLoader;
    FontLoader.get_fallback_font = &CFontLoaderCallbackAdapter::GetFallbackFont_Trampoline;
    FontLoader.get_fallback_font_for_characters = &CFontLoaderCallbackAdapter::GetFallbackFontForCharacters_Trampoline;
    FontLoader.load = &CFontLoaderCallbackAdapter::Load_Trampoline;

    ulPlatformSetFontLoader(FontLoader);
}

ULString CFontLoaderCallbackAdapter::GetFallbackFont_Trampoline()
{
    const std::shared_ptr<CFontLoaderCallbackAdapter> Self = Active();
    if (!Self)
        return ulCreateString("");

    const CJNICallScope CallScope;
    if (!CallScope)
        return ulCreateString("");

    const auto Result = static_cast<jstring>(CallScope->CallObjectMethod(Self->GetJavaInstance(), Self->m_GetFallbackFontMethodID));
    if (CallScope.CheckException())
        return ulCreateString("");

    // Ultralight calls ulDestroyString() on this, so it has to be a fresh allocation.
    return Result ? CJNIUtils::ToULString(CallScope.Env(), Result) : ulCreateString("Arial");
}

ULString CFontLoaderCallbackAdapter::GetFallbackFontForCharacters_Trampoline(ULString Characters, int Weight, bool bIsItalic)
{
    const std::shared_ptr<CFontLoaderCallbackAdapter> Self = Active();
    if (!Self)
        return ulCreateString("");

    const CJNICallScope CallScope;
    if (!CallScope)
        return ulCreateString("");

    const jstring CharactersString = CJNIUtils::ToJavaString(CallScope.Env(), Characters);
    const auto Result = static_cast<jstring>(CallScope->CallObjectMethod(Self->GetJavaInstance(), Self->m_GetFallbackFontForCharactersMethodID,
        CharactersString, static_cast<jint>(Weight), static_cast<jboolean>(bIsItalic)));

    if (CallScope.CheckException())
        return ulCreateString("");

    return Result ? CJNIUtils::ToULString(CallScope.Env(), Result) : ulCreateString("Arial");
}

ULFontFile CFontLoaderCallbackAdapter::Load_Trampoline(ULString Family, int Weight, bool bIsItalic)
{
    const std::shared_ptr<CFontLoaderCallbackAdapter> Self = Active();
    if (!Self)
        return nullptr;

    const CJNICallScope CallScope;
    if (!CallScope)
        return nullptr;

    const jstring FamilyString = CJNIUtils::ToJavaString(CallScope.Env(), Family);
    const jobject FontFile = CallScope->CallObjectMethod(Self->GetJavaInstance(), Self->m_LoadMethodID, FamilyString, static_cast<jint>(Weight), static_cast<jboolean>(bIsItalic));

    // A null font file is valid and means "fall back to another font".
    if (CallScope.CheckException() || !FontFile)
        return nullptr;

    // ULFontFile.getSource() decides which way the native font gets created.
    const jobject Source = CallScope->CallObjectMethod(FontFile, Self->m_FontFileGetSourceMethodID);
    if (CallScope.CheckException())
        return nullptr;

    if (CallScope->IsSameObject(Source, Self->m_SourceFilePath))
    {
        const auto Path = static_cast<jstring>(CallScope->CallObjectMethod(FontFile, Self->m_FontFileGetFilePathMethodID));
        if (CallScope.CheckException() || !Path)
            return nullptr;

        const ULString NativePath = CJNIUtils::ToULString(CallScope.Env(), Path);
        const ULFontFile Result = ulFontFileCreateFromFilePath(NativePath);

        ulDestroyString(NativePath);

        return Result;
    }

    const auto ByteArray = static_cast<jbyteArray>(CallScope->CallObjectMethod(FontFile, Self->m_FontFileGetBufferMethodID));
    if (CallScope.CheckException() || !ByteArray)
        return nullptr;

    jbyte* Bytes = CallScope->GetByteArrayElements(ByteArray, nullptr);
    if (!Bytes)
        return nullptr;

    const ULBuffer Buffer = ulCreateBufferFromCopy(Bytes, static_cast<size_t>(CallScope->GetArrayLength(ByteArray)));

    CallScope->ReleaseByteArrayElements(ByteArray, Bytes, JNI_ABORT);

    // Ultralight takes ownership of the buffer.
    return ulFontFileCreateFromBuffer(Buffer);
}
