package com.emarsys.mobileengage.notification

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class LaunchActivityCommandLifecycleCallbacksTest {

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
        mockPackageManager = mockk(relaxed = true)
        every { mockPackageManager.getLaunchIntentForPackage(any()) } returns launchIntent

    }

    @Test
    fun testOnResume_withCorrectActivity() {

        val mockActivity: Activity = mockk(relaxed = true)
        every { mockActivity.packageManager } returns mockPackageManager
        every { mockActivity.packageName } returns "com.emarsys.testUtil.fake"
        every { mockActivity.localClassName } returns "FakeActivity"

        LaunchActivityCommandLifecycleCallbacks(latch).onActivityResumed(mockActivity)

        latch.count shouldBe 0
    }
}