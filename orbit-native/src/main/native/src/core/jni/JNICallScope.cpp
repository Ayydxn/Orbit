#include "JNICallScope.h"

CJNICallScope::CJNICallScope()
    : m_Environment(CJNIThread::GetEnvironment())
{
    if (m_Environment)
        bFramePushed = m_Environment->PushLocalFrame(32) == JNI_OK;
}

CJNICallScope::~CJNICallScope()
{
    if (!bFramePushed)
        return;

    CJNIThread::ReportException(m_Environment);

    m_Environment->PopLocalFrame(nullptr);
}
