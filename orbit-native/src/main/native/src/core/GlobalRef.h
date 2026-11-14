#pragma once

#include "jni/JNIThread.h"

#include <jni.h>

#include <utility>

template<typename T>
class TGlobalRef
{
public:
    TGlobalRef() = default;

    // Creates a global reference from the given reference. The given reference is left untouched and still belongs to the caller.
    TGlobalRef(JNIEnv* Environment, T Ref)
        : m_Ref(Ref ? static_cast<T>(Environment->NewGlobalRef(Ref)) : nullptr) {}

    TGlobalRef(const TGlobalRef&) = delete;
    TGlobalRef& operator=(const TGlobalRef&) = delete;

    TGlobalRef(TGlobalRef&& Other) noexcept
        : m_Ref(std::exchange(Other.m_Ref, nullptr)) {}

    TGlobalRef& operator=(TGlobalRef&& Other) noexcept
    {
        if (this != &Other)
        {
            Reset();

            m_Ref = std::exchange(Other.m_Ref, nullptr);
        }

        return *this;
    }

    ~TGlobalRef() { Reset(); }

    void Reset()
    {
        if (!m_Ref)
            return;

        // If the JVM is already gone there's nothing left to release this into.
        if (JNIEnv* Environment = CJNIThread::GetEnvironment())
            Environment->DeleteGlobalRef(m_Ref);

        m_Ref = nullptr;
    }

    T Get() const { return m_Ref; }

    operator T() const { return m_Ref; }
private:
    T m_Ref = nullptr;
};
