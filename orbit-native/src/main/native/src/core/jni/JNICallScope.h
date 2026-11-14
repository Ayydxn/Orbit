#pragma once

#include "JNIThread.h"

#include <jni.h>

class CJNICallScope
{
public:
    CJNICallScope();
    ~CJNICallScope();

    CJNICallScope(const CJNICallScope&) = delete;
    CJNICallScope& operator=(const CJNICallScope&) = delete;

    explicit operator bool() const { return m_Environment != nullptr && bFramePushed; }

    JNIEnv* Env() const { return m_Environment; }
    JNIEnv* operator->() const { return m_Environment; }

    // Reports and clears a pending Java exception. Returns true if there was one, in which case the callback should bail out.
    bool CheckException() const { return CJNIThread::ReportException(m_Environment); }
private:
    JNIEnv* m_Environment = nullptr;
    bool bFramePushed = false;
};
