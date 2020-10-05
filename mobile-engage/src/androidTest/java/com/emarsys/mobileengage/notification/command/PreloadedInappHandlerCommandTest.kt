package com.emarsys.mobileengage.notification.command

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.testUtil.FileTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.File
import java.util.concurrent.CountDownLatch


class PreloadedInappHandlerCommandTest {
    companion object {
        private const val URL = "https://www.google.com"
    }

    private lateinit var mockDependencyContainer: MobileEngageDependencyContainer
    private lateinit var mockActivityLifecycleWatchdog: ActivityLifecycleWatchdog
    private lateinit var mockCoreSdkHandler: Handler
    private lateinit var fileUrl: String

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @Before
    fun setUp() {
        fileUrl = InstrumentationRegistry.getTargetContext().applicationContext.cacheDir.absolutePath + "/test.file"

        mockCoreSdkHandler = CoreSdkHandlerProvider().provideHandler()

        mockActivityLifecycleWatchdog = mock()
        val mockFileDownloader: FileDownloader = mock {
            on { readFileIntoString(any()) } doReturn "html"
            on { readURLIntoString(any()) } doReturn "html"
        }

        mockDependencyContainer = FakeMobileEngageDependencyContainer(
                activityLifecycleWatchdog = mockActivityLifecycleWatchdog,
                coreSdkHandler = mockCoreSdkHandler,
                fileDownloader = mockFileDownloader
        )

        DependencyInjection.setup(mockDependencyContainer)

    }

    @After
    fun tearDown() {
        application.unregisterActivityLifecycleCallbacks(mockActivityLifecycleWatchdog)
        mockCoreSdkHandler.looper.quitSafely()
        DependencyInjection.tearDown()
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailable() {
        FileTestUtils.writeToFile("test", fileUrl)

        val inapp = JSONObject().apply {
            put("campaignId", "campaignId")
            put("fileUrl", fileUrl)
        }
        val ems = JSONObject().apply {
            put("inapp", inapp.toString())
        }
        val payload = Bundle().apply {
            putString("ems", ems.toString())
        }
        val intent = Intent().apply {
            putExtra("payload", payload)
        }

        PreloadedInappHandlerCommand(intent).run()

        waitForEventLoopToFinish(mockCoreSdkHandler)

        verify(mockActivityLifecycleWatchdog).addTriggerOnActivityAction(any())
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailableButTheFileIsMissing() {
        FileTestUtils.writeToFile("test", fileUrl)
        File(fileUrl).delete()

        val inapp = JSONObject().apply {
            put("campaignId", "campaignId")
            put("fileUrl", fileUrl)
            put("url", URL)
        }
        val ems = JSONObject().apply {
            put("inapp", inapp.toString())
        }
        val payload = Bundle().apply {
            putString("ems", ems.toString())
        }
        val intent = Intent().apply {
            putExtra("payload", payload)
        }

        PreloadedInappHandlerCommand(intent).run()

        waitForEventLoopToFinish(mockCoreSdkHandler)

        verify(mockActivityLifecycleWatchdog).addTriggerOnActivityAction(any())
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsNotAvailable_butUrlIsAvailable() {
        val inapp = JSONObject().apply {
            put("campaignId", "campaignId")
            put("url", URL)
        }
        val ems = JSONObject().apply {
            put("inapp", inapp.toString())
        }
        val payload = Bundle().apply {
            putString("ems", ems.toString())
        }
        val intent = Intent().apply {
            putExtra("payload", payload)
        }

        PreloadedInappHandlerCommand(intent).run()

        waitForEventLoopToFinish(mockCoreSdkHandler)

        verify(mockActivityLifecycleWatchdog).addTriggerOnActivityAction(any())
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldDeleteFile_afterPushToInAppActionIsScheduled() {
        FileTestUtils.writeToFile("test", fileUrl)

        val inapp = JSONObject().apply {
            put("campaignId", "campaignId")
            put("fileUrl", fileUrl)
        }
        val ems = JSONObject().apply {
            put("inapp", inapp.toString())
        }
        val payload = Bundle().apply {
            putString("ems", ems.toString())
        }
        val intent = Intent().apply {
            putExtra("payload", payload)
        }

        File(fileUrl).exists() shouldBe true

        PreloadedInappHandlerCommand(intent).run()

        waitForEventLoopToFinish(mockCoreSdkHandler)

        verify(mockActivityLifecycleWatchdog).addTriggerOnActivityAction(any())

        File(fileUrl).exists() shouldBe false
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldNotScheduleInAppDisplay_ifInAppProperty_isMissing() {
        val ems = JSONObject()
        val payload = Bundle().apply {
            putString("ems", ems.toString())
        }
        val intent = Intent().apply {
            putExtra("payload", payload)
        }

        PreloadedInappHandlerCommand(intent).run()

        waitForEventLoopToFinish(mockCoreSdkHandler)

        verifyZeroInteractions(mockActivityLifecycleWatchdog)
    }

    private fun waitForEventLoopToFinish(handler: Handler) {
        val latch = CountDownLatch(1)
        handler.post { latch.countDown() }

        latch.await()
    }
}