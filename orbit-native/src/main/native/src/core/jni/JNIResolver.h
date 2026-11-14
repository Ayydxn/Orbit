// src/core/JNIResolver.h
#pragma once

#include "core/GlobalRef.h"

#include <string>
#include <unordered_map>
#include <vector>

class CJNIResolver
{
public:
    explicit CJNIResolver(JNIEnv* Environment)
        : m_Environment(Environment) {}

    TGlobalRef<jclass> Class(const char* ClassName);

    jmethodID Method(jclass Owner, const char* Name, const char* Signature);
    jmethodID StaticMethod(jclass Owner, const char* Name, const char* Signature);
    jfieldID Field(jclass Owner, const char* Name, const char* Signature);

    // Resolves a static object field like an enum constant and returns a global reference to its value.
    TGlobalRef<jobject> StaticObject(jclass Owner, const char* Name, const char* Signature);

    bool Succeeded() const { return m_Failures.empty(); }
    std::string DescribeFailures() const;
private:
    JNIEnv* m_Environment;
    std::vector<std::string> m_Failures;
    std::unordered_map<jclass, std::string> m_ClassNames;

    void RecordFailure(jclass Owner, const char* Name, const char* Signature);
};
