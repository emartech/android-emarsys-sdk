package com.emarsys.testUtil

import android.app.Application
import android.content.pm.ApplicationInfo
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

object ApplicationTestUtils {

    @JvmStatic
    val applicationDebug: Application
    get() {
        val applicationDebug = mock(Application::class.java, Mockito.RETURNS_DEEP_STUBS)
        val info = ApplicationInfo()
        info.flags = ApplicationInfo.FLAG_DEBUGGABLE
        `when`(applicationDebug.applicationInfo).thenReturn(info)
        return applicationDebug
    }

    @JvmStatic
    val applicationRelease: Application
    get() {
        val applicationRelease = mock(Application::class.java, Mockito.RETURNS_DEEP_STUBS)
        val info = ApplicationInfo()
        info.flags = 0
        `when`(applicationRelease.applicationInfo).thenReturn(info)
        return applicationRelease
    }

}
