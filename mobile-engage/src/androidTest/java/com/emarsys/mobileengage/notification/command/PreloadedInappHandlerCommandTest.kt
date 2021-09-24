package com.emarsys.mobileengage.notification.command

import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.di.MobileEngageComponent
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.testUtil.FileTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.io.File
import java.util.concurrent.CountDownLatch


class PreloadedInappHandlerCommandTest {
    companion object {
        private const val URL = "https://www.google.com"
    }

    private lateinit var mockDependencyContainer: MobileEngageComponent
    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var mockLifecycleActionRegistry: ActivityLifecycleActionRegistry
    private lateinit var fileUrl: String

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        fileUrl = InstrumentationRegistry.getTargetContext().applicationContext.cacheDir.absolutePath + "/test.file"

        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        mockLifecycleActionRegistry = mock()

        val mockFileDownloader: FileDownloader = mock {
            on { readFileIntoString(any()) } doReturn "html"
            on { readURLIntoString(any()) } doReturn "html"
        }

        mockDependencyContainer = FakeMobileEngageDependencyContainer(
                coreSdkHandler = coreSdkHandler,
                activityLifecycleActionRegistry = mockLifecycleActionRegistry,
                fileDownloader = mockFileDownloader
        )

        setupMobileEngageComponent(mockDependencyContainer)

    }

    @After
    fun tearDown() {
        coreSdkHandler.looper.quitSafely()
        tearDownMobileEngageComponent()
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

        waitForEventLoopToFinish(coreSdkHandler)

        verify(mockLifecycleActionRegistry).addTriggerOnActivityAction(any())
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

        waitForEventLoopToFinish(coreSdkHandler)

        verify(mockLifecycleActionRegistry).addTriggerOnActivityAction(any())
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

        waitForEventLoopToFinish(coreSdkHandler)

        verify(mockLifecycleActionRegistry).addTriggerOnActivityAction(any())
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

        waitForEventLoopToFinish(coreSdkHandler)

        verify(mockLifecycleActionRegistry).addTriggerOnActivityAction(any())

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

        waitForEventLoopToFinish(coreSdkHandler)

        verifyZeroInteractions(mockLifecycleActionRegistry)
    }

    private fun waitForEventLoopToFinish(handler: CoreSdkHandler) {
        val latch = CountDownLatch(1)
        handler.post { latch.countDown() }

        latch.await()
    }
}