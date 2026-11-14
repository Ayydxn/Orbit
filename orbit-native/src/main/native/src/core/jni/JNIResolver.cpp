#include "JNIResolver.h"
#include "core/ScopedLocalRef.h"

TGlobalRef<jclass> CJNIResolver::Class(const char* ClassName)
{
    const TScopedLocalRef LocalClass(m_Environment, m_Environment->FindClass(ClassName));
    if (!LocalClass)
    {
        m_Environment->ExceptionClear();

        m_Failures.push_back(std::string("Class not found: ") + ClassName);

        return {};
    }

    TGlobalRef<jclass> GlobalClass(m_Environment, LocalClass.Get());
    m_ClassNames[GlobalClass.Get()] = ClassName;

    return GlobalClass;
}

jmethodID CJNIResolver::Method(jclass Owner, const char* Name, const char* Signature)
{
    if (!Owner)
        return nullptr; // The class failing to resolve has already been recorded.

    const jmethodID MethodID = m_Environment->GetMethodID(Owner, Name, Signature);
    if (!MethodID)
        RecordFailure(Owner, Name, Signature);

    return MethodID;
}

jmethodID CJNIResolver::StaticMethod(jclass Owner, const char* Name, const char* Signature)
{
    if (!Owner)
        return nullptr;

    const jmethodID MethodID = m_Environment->GetStaticMethodID(Owner, Name, Signature);
    if (!MethodID)
        RecordFailure(Owner, Name, Signature);

    return MethodID;
}

jfieldID CJNIResolver::Field(jclass Owner, const char* Name, const char* Signature)
{
    if (!Owner)
        return nullptr;

    const jfieldID FieldID = m_Environment->GetFieldID(Owner, Name, Signature);
    if (!FieldID)
        RecordFailure(Owner, Name, Signature);

    return FieldID;
}

TGlobalRef<jobject> CJNIResolver::StaticObject(jclass Owner, const char* Name, const char* Signature)
{
    if (!Owner)
        return {};

    const jfieldID FieldID = m_Environment->GetStaticFieldID(Owner, Name, Signature);
    if (!FieldID)
    {
        RecordFailure(Owner, Name, Signature);

        return {};
    }

    const TScopedLocalRef Value(m_Environment, m_Environment->GetStaticObjectField(Owner, FieldID));

    return { m_Environment, Value.Get() };
}

std::string CJNIResolver::DescribeFailures() const
{
    std::string Description = "Failed to resolve the following Java members:";
    for (const std::string& Failure : m_Failures)
        Description += "\n  " + Failure;

    return Description;
}

void CJNIResolver::RecordFailure(jclass Owner, const char* Name, const char* Signature)
{
    m_Environment->ExceptionClear();

    const auto ClassName = m_ClassNames.find(Owner);

    m_Failures.push_back((ClassName != m_ClassNames.end() ? ClassName->second :
        std::string("<unknown class>")) + "." + Name + " " + Signature);
}
