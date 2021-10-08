package com.emarsys.core.concurrency

import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import android.os.HandlerThread
import io.kotlintest.shouldBe
import org.junit.*
import org.junit.rules.TestRule
import java.lang.RuntimeException
import java.util.concurrent.CountDownLatch

class CoreHandlerTest {
    private lateinit var handler: CoreHandler
    private lateinit var handlerThread: HandlerThread
    private lateinit var failingRunnable: Runnable

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun setUp() {
        val threadName = "test"
        handlerThread = HandlerThread(threadName)
        handlerThread.start()
        handler = CoreHandler(handlerThread)
        failingRunnable = Runnable { throw RuntimeException("error") }
    }

    @After
    fun tearDown() {
        handlerThread.quit()
    }

    @Test
    fun testConstructor_innerLooper_isInitialized() {
        Assert.assertNotNull(handler.looper)
        Assert.assertEquals(handlerThread.name, handler.looper.thread.name)
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