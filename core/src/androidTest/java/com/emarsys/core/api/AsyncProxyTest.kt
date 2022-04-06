package com.emarsys.core.api

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

class AsyncProxyTest {

    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @Test
    fun testInvoke_shouldInvokeMethod() {
        val expected: CharSequence = "test"

        val result = expected.proxyWithHandler(concurrentHandlerHolder)

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

        callback.proxyWithHandler(concurrentHandlerHolder).run()

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

        val result = callback.proxyWithHandler(concurrentHandlerHolder).call()

        result shouldBe "test"
        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testInvoke_shouldUseCoreThread_whenAlreadyOnCoreThread() {
        val threadSpy: ThreadSpy<Any> = ThreadSpy()

        val callback: Callable<String> = Callable<String> {
            threadSpy.call()
            "test"
        }
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.coreHandler.post {
            val result = callback.proxyWithHandler(concurrentHandlerHolder).call()
            result shouldBe "test"
            latch.countDown()
        }
        latch.await()
        threadSpy.verifyCalledOnCoreSdkThread()
    }
}