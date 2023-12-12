package com.emarsys.mobileengage.notification.command

import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.di.MobileEngageComponent
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.service.NotificationData
import com.emarsys.mobileengage.service.NotificationMethod
import com.emarsys.mobileengage.service.NotificationOperation
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.io.File
import java.util.concurrent.CountDownLatch


class PreloadedInappHandlerCommandTest {
    companion object {
        private const val URL = "https://www.google.com"
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val COLLAPSE_ID = "testCollapseId"
        const val MULTICHANNEL_ID = "test multiChannel id"
        const val SID = "test sid"
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456
        val notificationData = NotificationData(
            null,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_RESOURCE_ID,
            colorResourceId = COLOR_RESOURCE_ID,
            notificationMethod = NotificationMethod(COLLAPSE_ID, NotificationOperation.UPDATE),
            actions = null,
            inapp = null
        )
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
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
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
        concurrentHandlerHolder.coreLooper.quitSafely()
        tearDownMobileEngageComponent()
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailable() {
        FileTestUtils.writeToFile("test", fileUrl)

        val inapp = JSONObject().apply {
            put("campaignId", "campaignId")
            put("fileUrl", fileUrl)
        }

        val testNotificationData = notificationData.copy(inapp = inapp.toString())

        PreloadedInappHandlerCommand(testNotificationData).run()

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
        val testNotificationData = notificationData.copy(inapp = inapp.toString())

        PreloadedInappHandlerCommand(testNotificationData).run()

        waitForEventLoopToFinish(concurrentHandlerHolder)

        verify(mockLifecycleActionRegistry).addTriggerOnActivityAction(any())
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsNotAvailable_butUrlIsAvailable() {
        val inapp = JSONObject().apply {
            put("campaignId", "campaignId")
            put("url", URL)
        }
        val testNotificationData = notificationData.copy(inapp = inapp.toString())

        PreloadedInappHandlerCommand(testNotificationData).run()

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
        val testNotificationData = notificationData.copy(inapp = inapp.toString())

        File(fileUrl).exists() shouldBe true

        PreloadedInappHandlerCommand(testNotificationData).run()

        waitForEventLoopToFinish(concurrentHandlerHolder)

        verify(mockLifecycleActionRegistry).addTriggerOnActivityAction(any())

        File(fileUrl).exists() shouldBe false
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldNotScheduleInAppDisplay_ifInAppProperty_isMissing() {
        PreloadedInappHandlerCommand(notificationData).run()

        waitForEventLoopToFinish(concurrentHandlerHolder)

        verifyNoInteractions(mockLifecycleActionRegistry)
    }

    private fun waitForEventLoopToFinish(handlerHolder: ConcurrentHandlerHolder) {
        val latch = CountDownLatch(1)
        handlerHolder.coreHandler.post { latch.countDown() }

        latch.await()
    }
}