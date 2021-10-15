package com.emarsys.core.api

import android.os.HandlerThread
import com.emarsys.core.concurrency.CoreHandler
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

class AsyncProxyTest {

    private lateinit var handler: CoreSdkHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        handler = CoreSdkHandler(CoreHandler(handlerThread))
    }

    @Test
    fun testInvoke_shouldInvokeMethod() {
        val expected: CharSequence = "test"

        val result = expected.proxyWithHandler(handler)

        result.toString() shouldBe "test"
    }

    @Test
    fun testInvoke_shouldDelegateToHandlerThread() {
        val threadSpy: ThreadSpy<Any> = ThreadSpy()
        val latch = CountDownLatch(1)

        val callback = Runnable {
            threadSpy.call()
            latch.countDown()
        }

        callback.proxyWithHandler(handler).run()

        latch.await()
        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testInvoke_shouldDelegateToHandlerThread_andWaitIfNotVoid() {
        val threadSpy: ThreadSpy<Any> = ThreadSpy()

        val callback: Callable<String> = Callable<String> {
            threadSpy.call()
            "test"
        }

        val result = callback.proxyWithHandler(handler).call()

        result shouldBe "test"
        threadSpy.verifyCalledOnCoreSdkThread()
    }
}