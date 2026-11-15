#include "FileSystemCallbackAdapter.h"
#include "core/GlobalRef.h"
#include "core/jni/JNICallScope.h"

#include <Ultralight/CAPI/CAPI_Buffer.h>
#include <Ultralight/CAPI/CAPI_Platform.h>

void CFileSystemCallbackAdapter::Resolve(CJNIResolver& Resolver)
{
    const TGlobalRef<jclass> FileSystemClass = Resolver.Class("me/ayydxn/orbit/ultralight/platform/ULFileSystem");

    m_FileExistsMethodID = Resolver.Method(FileSystemClass, "fileExists", "(Ljava/lang/String;)Z");
    m_GetFileMimeTypeMethodID = Resolver.Method(FileSystemClass, "getFileMimeType", "(Ljava/lang/String;)Ljava/lang/String;");
    m_GetFileCharsetMethodID = Resolver.Method(FileSystemClass, "getFileCharset", "(Ljava/lang/String;)Ljava/lang/String;");
    m_OpenFileMethodID = Resolver.Method(FileSystemClass, "openFile", "(Ljava/lang/String;)[B");
}

void CFileSystemCallbackAdapter::Apply()
{
    ULFileSystem FileSystem;
    FileSystem.file_exists = &CFileSystemCallbackAdapter::FileExists_Trampoline;
    FileSystem.get_file_mime_type = &CFileSystemCallbackAdapter::GetFileMimeType_Trampoline;
    FileSystem.get_file_charset = &CFileSystemCallbackAdapter::GetFileCharset_Trampoline;
    FileSystem.open_file = &CFileSystemCallbackAdapter::OpenFile_Trampoline;

    ulPlatformSetFileSystem(FileSystem);
}

bool CFileSystemCallbackAdapter::FileExists_Trampoline(ULString Path)
{
    const std::shared_ptr<CFileSystemCallbackAdapter> Self = Active();
    if (!Self)
        return false;

    const CJNICallScope CallScope;
    if (!CallScope)
        return false;

    const jstring PathString = CJNIUtils::ToJavaString(CallScope.Env(), Path);
    const jboolean Result = CallScope->CallBooleanMethod(Self->GetJavaInstance(), Self->m_FileExistsMethodID, PathString);

    return !CallScope.CheckException() && Result == JNI_TRUE;
}

ULString CFileSystemCallbackAdapter::GetFileMimeType_Trampoline(ULString Path)
{
    const std::shared_ptr<CFileSystemCallbackAdapter> Self = Active();
    if (!Self)
        return ulCreateString("");

    return Self->CallStringFunction(Self->m_GetFileMimeTypeMethodID, Path, "application/unknown");
}

ULString CFileSystemCallbackAdapter::GetFileCharset_Trampoline(ULString Path)
{
    const std::shared_ptr<CFileSystemCallbackAdapter> Self = Active();
    if (!Self)
        return ulCreateString("");

    return Self->CallStringFunction(Self->m_GetFileCharsetMethodID, Path, "utf-8");
}

ULBuffer CFileSystemCallbackAdapter::OpenFile_Trampoline(ULString Path)
{
    const std::shared_ptr<CFileSystemCallbackAdapter> Self = Active();
    if (!Self)
        return nullptr;

    const CJNICallScope CallScope;
    if (!CallScope)
        return nullptr;

    const jstring PathString = CJNIUtils::ToJavaString(CallScope.Env(), Path);
    const auto ByteArray = static_cast<jbyteArray>(CallScope->CallObjectMethod(Self->GetJavaInstance(), Self->m_OpenFileMethodID, PathString));
    if (CallScope.CheckException() || !ByteArray)
        return nullptr;

    jbyte* Bytes = CallScope->GetByteArrayElements(ByteArray, nullptr);
    if (!Bytes)
        return nullptr;

    const ULBuffer Buffer = ulCreateBufferFromCopy(Bytes, static_cast<size_t>(CallScope->GetArrayLength(ByteArray)));

    CallScope->ReleaseByteArrayElements(ByteArray, Bytes, JNI_ABORT);

    return Buffer;
}

ULString CFileSystemCallbackAdapter::CallStringFunction(jmethodID MethodID, ULString Path, const char* NullFallback) const
{
    const CJNICallScope CallScope;
    if (!CallScope)
        return ulCreateString("");

    const jstring PathString = CJNIUtils::ToJavaString(CallScope.Env(), Path);
    const auto Result = static_cast<jstring>(CallScope->CallObjectMethod(GetJavaInstance(), MethodID, PathString));

    if (CallScope.CheckException())
        return ulCreateString("");

    return Result ? CJNIUtils::ToULString(CallScope.Env(), Result) : ulCreateString(NullFallback);
}
