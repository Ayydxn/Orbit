#pragma once

#include "core/CallbackAdapter.h"
#include "core/GlobalRef.h"

#include <Ultralight/CAPI/CAPI_FontLoader.h>

class CFontLoaderCallbackAdapter final : public TPlatformCallbackAdapter<CFontLoaderCallbackAdapter>
{
    friend class TPlatformCallbackAdapter<CFontLoaderCallbackAdapter>;
public:
    CFontLoaderCallbackAdapter(JNIEnv* Environment, jobject JavaInstance)
        : TPlatformCallbackAdapter(Environment, JavaInstance) {}
private:
    void Resolve(CJNIResolver& Resolver);
    void Apply();

    static ULString GetFallbackFont_Trampoline();
    static ULString GetFallbackFontForCharacters_Trampoline(ULString Characters, int Weight, bool bIsItalic);
    static ULFontFile Load_Trampoline(ULString Family, int Weight, bool bIsItalic);
private:
    TGlobalRef<jobject> m_SourceFilePath;

    jmethodID m_GetFallbackFontMethodID = nullptr;
    jmethodID m_GetFallbackFontForCharactersMethodID = nullptr;
    jmethodID m_LoadMethodID = nullptr;

    jmethodID m_FontFileGetSourceMethodID = nullptr;
    jmethodID m_FontFileGetFilePathMethodID = nullptr;
    jmethodID m_FontFileGetBufferMethodID = nullptr;
};
