package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.mockito.ThreadSpy
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class AppEventCommandTest {
    @Rule
    @JvmField
    val timeout: TestRule = timeoutRule

    private lateinit var applicationContext: Context
    private lateinit var eventHandlerProvider: EventHandlerProvider
    private lateinit var uiHandler: Handler
    private lateinit var mockEventHandler: EventHandler

    @Before
    fun setUp() {
        applicationContext = getTargetContext().applicationContext
        mockEventHandler = mock()
        eventHandlerProvider = mock()
        uiHandler = Handler(Looper.getMainLooper())
        whenever(eventHandlerProvider.eventHandler).thenReturn(mockEventHandler)
    }

    @Test
    @Throws(JSONException::class)
    fun testRun_invokeHandleEventMethod_onNotificationEventHandlerOnMainThread() {
        val name = "nameOfTheEvent"
        val payload = JSONObject()
                .put("payloadKey", "payloadValue")
        val latch = CountDownLatch(1)

        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockEventHandler.handleEvent(anyOrNull(), anyOrNull(), anyOrNull())).doAnswer(threadSpy)

        AppEventCommand(applicationContext, eventHandlerProvider, uiHandler, name, payload).run()
        uiHandler.post {
            latch.countDown()
        }
        latch.await()

        verify(mockEventHandler).handleEvent(applicationContext, name, payload)
        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    @Throws(JSONException::class)
    fun testRun_invokeHandleEventMethod_onNotificationEventHandler_whenThereIsNoPayload() {
        val name = "nameOfTheEvent"

        val latch = CountDownLatch(1)

        AppEventCommand(applicationContext, eventHandlerProvider, uiHandler, name, null).run()
        uiHandler.post {
            latch.countDown()
        }
        latch.await()

        verify(mockEventHandler).handleEvent(applicationContext, name, null)
    }

    @Test
    fun testRun_shouldIgnoreHandler_ifNull() {
        try {
            AppEventCommand(applicationContext, eventHandlerProvider, uiHandler, "", null).run()
        } catch (e: Exception) {
            Assert.fail(e.message)
        }
    }
}