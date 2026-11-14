#pragma once

#include <jni.h>

template<typename T>
class TScopedLocalRef
{
public:
    TScopedLocalRef(JNIEnv* Environment, T Ref)
        : m_Environment(Environment), m_Ref(Ref) {}

    TScopedLocalRef(const TScopedLocalRef&) = delete;
    TScopedLocalRef& operator=(const TScopedLocalRef&) = delete;

    ~TScopedLocalRef()
    {
        if (m_Ref)
            m_Environment->DeleteLocalRef(m_Ref);
    }

    T Get() const { return m_Ref; }

    operator T() const { return m_Ref; }
private:
    JNIEnv* m_Environment;
    T m_Ref;
};
