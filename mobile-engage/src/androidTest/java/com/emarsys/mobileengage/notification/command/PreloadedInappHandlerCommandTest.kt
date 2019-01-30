package com.emarsys.mobileengage.notification.command

import android.content.Intent
import android.os.Build.VERSION_CODES.KITKAT
import android.os.Bundle
import android.os.Handler
import androidx.test.filters.SdkSuppress
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.FileUtils
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.iam.PushToInAppAction
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.io.File
import java.util.concurrent.CountDownLatch

@SdkSuppress(minSdkVersion = KITKAT)
class PreloadedInappHandlerCommandTest {
    companion object {
        private const val URL = "https://www.google.com"
    }

    private lateinit var dependencyContainer: MobileEngageDependencyContainer
    private lateinit var activityLifecycleWatchdog: ActivityLifecycleWatchdog
    private lateinit var coreSdkHandler: Handler

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        activityLifecycleWatchdog = mock(ActivityLifecycleWatchdog::class.java)

        dependencyContainer = mock(MobileEngageDependencyContainer::class.java)
        whenever(dependencyContainer.activityLifecycleWatchdog).thenReturn(activityLifecycleWatchdog)
        whenever(dependencyContainer.inAppPresenter).thenReturn(mock(InAppPresenter::class.java))
        whenever(dependencyContainer.timestampProvider).thenReturn(mock(TimestampProvider::class.java))
        whenever(dependencyContainer.coreSdkHandler).thenReturn(coreSdkHandler)
    }

    @After
    fun tearDown() {
        coreSdkHandler.looper.quit()
        DependencyInjection.tearDown()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHandlePreloadedInAppMessage_intentMustNotBeNull() {
        PreloadedInappHandlerCommand(null, dependencyContainer)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHandlePreloadedInAppMessage_dependencyContainerMustNotBeNull() {
        PreloadedInappHandlerCommand(Intent(), null)
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailable() {
        val intent = Intent()
        val payload = Bundle()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaignId", "campaignId")
        inapp.put("fileUrl", FileUtils.download(InstrumentationRegistry.getTargetContext(), URL))
        ems.put("inapp", inapp.toString())
        payload.putString("ems", ems.toString())
        intent.putExtra("payload", payload)

        PreloadedInappHandlerCommand(intent, dependencyContainer).run()

        waitForEventLoopToFinish(coreSdkHandler)

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction::class.java))
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailableButTheFileIsMissing() {
        val intent = Intent()
        val payload = Bundle()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaignId", "campaignId")
        val fireUrl = FileUtils.download(InstrumentationRegistry.getTargetContext(), URL)
        File(fireUrl).delete()
        inapp.put("fileUrl", fireUrl)
        inapp.put("url", URL)
        ems.put("inapp", inapp.toString())
        payload.putString("ems", ems.toString())
        intent.putExtra("payload", payload)

        PreloadedInappHandlerCommand(intent, dependencyContainer).run()

        waitForEventLoopToFinish(coreSdkHandler)

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction::class.java))
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsNotAvailable_butUrlIsAvailable() {
        val intent = Intent()
        val payload = Bundle()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaignId", "campaignId")
        inapp.put("url", URL)
        ems.put("inapp", inapp.toString())
        payload.putString("ems", ems.toString())
        intent.putExtra("payload", payload)

        PreloadedInappHandlerCommand(intent, dependencyContainer).run()

        waitForEventLoopToFinish(coreSdkHandler)

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction::class.java))
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldDeleteFile_afterPushToInAppActionIsScheduled() {
        val fileUrl = FileUtils.download(InstrumentationRegistry.getTargetContext(), URL)

        val intent = Intent()
        val payload = Bundle()
        val ems = JSONObject()
        val inapp = JSONObject()
        inapp.put("campaignId", "campaignId")
        inapp.put("fileUrl", fileUrl)
        ems.put("inapp", inapp.toString())
        payload.putString("ems", ems.toString())
        intent.putExtra("payload", payload)

        assertEquals(true, File(fileUrl).exists())

        PreloadedInappHandlerCommand(intent, dependencyContainer).run()

        waitForEventLoopToFinish(coreSdkHandler)

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction::class.java))

        assertEquals(false, File(fileUrl).exists())
    }

    @Test
    fun testHandlePreloadedInAppMessage_shouldNotScheduleInAppDisplay_ifInAppProperty_isMissing() {
        val intent = Intent()
        val payload = Bundle()
        val ems = JSONObject()
        payload.putString("ems", ems.toString())
        intent.putExtra("payload", payload)

        PreloadedInappHandlerCommand(intent, dependencyContainer).run()

        waitForEventLoopToFinish(coreSdkHandler)

        verifyZeroInteractions(activityLifecycleWatchdog)
    }

    private fun waitForEventLoopToFinish(handler: Handler) {
        val latch = CountDownLatch(1)
        handler.post { latch.countDown() }

        latch.await()
    }
}