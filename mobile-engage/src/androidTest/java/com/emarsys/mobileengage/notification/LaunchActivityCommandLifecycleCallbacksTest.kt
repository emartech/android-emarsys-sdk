package com.emarsys.mobileengage.notification

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class LaunchActivityCommandLifecycleCallbacksTest  {

    private lateinit var latch: CountDownLatch
    private lateinit var launchIntent: Intent
    private lateinit var mockPackageManager: PackageManager

    @Before
    fun setUp() {
        latch = CountDownLatch(1)
        launchIntent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FakeActivity::class.java
        )
        mockPackageManager = mock {
            on { getLaunchIntentForPackage(any()) } doReturn launchIntent
        }
    }

    @Test
    fun testOnResume_withCorrectActivity() {

        val mockActivity: Activity = mock {
            on { packageManager } doReturn mockPackageManager
            on { packageName } doReturn "com.emarsys.testUtil.fake"
            on { localClassName } doReturn "FakeActivity"
        }

        LaunchActivityCommandLifecycleCallbacks(latch).onActivityResumed(mockActivity)

        latch.count shouldBe 0
    }
}