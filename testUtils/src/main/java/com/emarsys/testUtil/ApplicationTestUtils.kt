package com.emarsys.testUtil

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import io.mockk.every
import io.mockk.spyk

object ApplicationTestUtils {

    @JvmStatic
    val applicationDebug: Application
        get() = getApplication { flags = ApplicationInfo.FLAG_DEBUGGABLE }

    @JvmStatic
    val applicationRelease: Application
        get() = getApplication { flags = 0 }

    private fun getApplication(init: ApplicationInfo.() -> Unit) =
        (spyk<Context>(InstrumentationRegistry.getTargetContext().applicationContext) as Application).also {
            every { it.applicationInfo } returns ApplicationInfo().apply(init)
        }

}
