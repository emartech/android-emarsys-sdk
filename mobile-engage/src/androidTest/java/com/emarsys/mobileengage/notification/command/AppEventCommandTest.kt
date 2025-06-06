package com.emarsys.mobileengage.notification.command

import android.content.Context
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.assertions.fail
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class AppEventCommandTest {

    private lateinit var applicationContext: Context
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockEventHandler: CacheableEventHandler

    @Before
    fun setUp() {
        applicationContext = getTargetContext().applicationContext
        mockEventHandler = mockk(relaxed = true)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @Test
    @Throws(JSONException::class)
    fun testRun_invokeHandleEventMethod_onNotificationEventHandlerOnMainThread() {
        val name = "nameOfTheEvent"
        val payload = JSONObject()
            .put("payloadKey", "payloadValue")
        val latch = CountDownLatch(1)

        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        every { mockEventHandler.handleEvent(any(), any(), any()) } answers {
            threadSpy.call()
        }

        AppEventCommand(
            applicationContext,
            mockEventHandler,
            concurrentHandlerHolder,
            name,
            payload
        ).run()
        concurrentHandlerHolder.postOnMain {
            latch.countDown()
        }
        latch.await()

        verify { mockEventHandler.handleEvent(applicationContext, name, payload) }
        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    @Throws(JSONException::class)
    fun testRun_invokeHandleEventMethod_onNotificationEventHandler_whenThereIsNoPayload() {
        val name = "nameOfTheEvent"

        val latch = CountDownLatch(1)

        AppEventCommand(
            applicationContext,
            mockEventHandler,
            concurrentHandlerHolder,
            name,
            null
        ).run()
        concurrentHandlerHolder.postOnMain {
            latch.countDown()
        }
        latch.await()

        verify { mockEventHandler.handleEvent(applicationContext, name, null) }
    }

    @Test
    fun testRun_shouldIgnoreHandler_ifNull() {
        try {
            AppEventCommand(
                applicationContext,
                mockEventHandler,
                concurrentHandlerHolder,
                "",
                null
            ).run()
        } catch (e: Exception) {
            fail(e.message!!)
        }
    }
}