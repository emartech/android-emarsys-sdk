package com.emarsys.mobileengage.notification.command

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
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
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockLifecycleActionRegistry: ActivityLifecycleActionRegistry
    private lateinit var fileUrl: String

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        fileUrl =
            InstrumentationRegistry.getTargetContext().applicationContext.cacheDir.absolutePath + "/test.file"
        val uiHandler = Handler(Looper.getMainLooper())
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory(uiHandler).create()
        mockLifecycleActionRegistry = mock()

        val mockFileDownloader: FileDownloader = mock {
            on { readFileIntoString(any()) } doReturn "html"
            on { readURLIntoString(any()) } doReturn "html"
        }

        mockDependencyContainer = FakeMobileEngageDependencyContainer(
            concurrentHandlerHolder = concurrentHandlerHolder,
            activityLifecycleActionRegistry = mockLifecycleActionRegistry,
            fileDownloader = mockFileDownloader
        )

        setupMobileEngageComponent(mockDependencyContainer)

    }

    @After
    fun tearDown() {
        concurrentHandlerHolder.looper.quitSafely()
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

        waitForEventLoopToFinish(concurrentHandlerHolder)

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

        waitForEventLoopToFinish(concurrentHandlerHolder)

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

        waitForEventLoopToFinish(concurrentHandlerHolder)

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

        waitForEventLoopToFinish(concurrentHandlerHolder)

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

        waitForEventLoopToFinish(concurrentHandlerHolder)

        verifyNoInteractions(mockLifecycleActionRegistry)
    }

    private fun waitForEventLoopToFinish(handlerHolder: ConcurrentHandlerHolder) {
        val latch = CountDownLatch(1)
        handlerHolder.coreHandler.post { latch.countDown() }

        latch.await()
    }
}