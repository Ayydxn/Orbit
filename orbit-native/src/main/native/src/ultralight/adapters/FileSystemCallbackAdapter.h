#pragma once

#include "core/CallbackAdapter.h"

#include <Ultralight/CAPI/CAPI_FileSystem.h>

class CFileSystemCallbackAdapter final : public TPlatformCallbackAdapter<CFileSystemCallbackAdapter>
{
    friend class TPlatformCallbackAdapter<CFileSystemCallbackAdapter>;
public:
    CFileSystemCallbackAdapter(JNIEnv* Environment, jobject JavaInstance)
        : TPlatformCallbackAdapter(Environment, JavaInstance) {}
private:
    void Resolve(CJNIResolver& Resolver);
    void Apply();

    static bool FileExists_Trampoline(ULString Path);
    static ULString GetFileMimeType_Trampoline(ULString Path);
    static ULString GetFileCharset_Trampoline(ULString Path);
    static ULBuffer OpenFile_Trampoline(ULString Path);

    ULString CallStringFunction(jmethodID MethodID, ULString Path, const char* NullFallback) const;
private:
    jmethodID m_FileExistsMethodID = nullptr;
    jmethodID m_GetFileMimeTypeMethodID = nullptr;
    jmethodID m_GetFileCharsetMethodID = nullptr;
    jmethodID m_OpenFileMethodID = nullptr;
};
