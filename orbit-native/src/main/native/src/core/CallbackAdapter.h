// src/core/CallbackAdapter.h
#pragma once

#include "GlobalRef.h"
#include "jni/JNIResolver.h"
#include "utils/JNIUtils.h"

#include <jni.h>

#include <memory>
#include <mutex>

// Holds the Java object that native callbacks are forwarded to.
class ICallbackAdapter
{
public:
    ICallbackAdapter(JNIEnv* Environment, jobject JavaInstance)
        : m_JavaInstance(Environment, JavaInstance) {}

    ICallbackAdapter(const ICallbackAdapter&) = delete;
    ICallbackAdapter& operator=(const ICallbackAdapter&) = delete;

    virtual ~ICallbackAdapter() = default;

    jobject GetJavaInstance() const { return m_JavaInstance.Get(); }
private:
    TGlobalRef<jobject> m_JavaInstance;
};

template <typename TDerived>
class TPlatformCallbackAdapter : public ICallbackAdapter
{
public:
    // Returns false, with a Java exception pending, if something the adapter needs couldn't be resolved.
    static bool Install(JNIEnv* Environment, jobject JavaInstance)
    {
        auto Adapter = std::make_shared<TDerived>(Environment, JavaInstance);

        CJNIResolver Resolver(Environment);
        Adapter->Resolve(Resolver);

        if (!Resolver.Succeeded())
        {
            CJNIUtils::ThrowJavaException(Environment, "java/lang/IllegalStateException", Resolver.DescribeFailures());

            return false;
        }

        // The slot is filled before Ultralight learns about the trampolines, so a callback can never find it empty.
        // A replaced adapter stays alive for as long as a callback that already picked it up is running.
        SetActive(Adapter);
        Adapter->Apply();

        return true;
    }

    static void Release()
    {
        SetActive(nullptr);
    }
protected:
    using ICallbackAdapter::ICallbackAdapter;

    static std::shared_ptr<TDerived> Active()
    {
        std::scoped_lock Lock(s_Mutex);

        return s_Active;
    }
private:
    inline static std::mutex s_Mutex;
    inline static std::shared_ptr<TDerived> s_Active;

    static void SetActive(std::shared_ptr<TDerived> Adapter)
    {
        std::shared_ptr<TDerived> Previous; // Released outside the lock since its destructor reaches into the JVM.

        {
            std::scoped_lock Lock(s_Mutex);

            Previous = std::move(s_Active);
            s_Active = std::move(Adapter);
        }
    }
};
