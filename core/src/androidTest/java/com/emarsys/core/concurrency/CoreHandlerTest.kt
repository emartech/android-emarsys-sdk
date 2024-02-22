package com.emarsys.core.concurrency

import android.os.HandlerThread
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class CoreHandlerTest {
    private lateinit var handler: CoreHandler
    private lateinit var handlerThread: HandlerThread
    private lateinit var failingRunnable: Runnable

    @BeforeEach
    fun setUp() {
        val threadName = "test"
        handlerThread = HandlerThread(threadName)
        handlerThread.start()
        handler = CoreHandler(handlerThread)
        failingRunnable = Runnable { throw RuntimeException("error") }
    }

    @AfterEach
    fun tearDown() {
        handlerThread.quit()
    }

    @Test
    fun testConstructor_innerLooper_isInitialized() {
        handler.looper shouldNotBe null
        handlerThread.name shouldBe handler.looper.thread.name
    }

    @Test
    fun testDispatchMessage_shouldBeResilient_toExceptions() {
        val latch = CountDownLatch(1)
        handler.post(failingRunnable)
        handler.post(failingRunnable)
        handler.post { latch.countDown() }
        latch.await()
        handler.looper.thread.isAlive shouldBe true
    }
}