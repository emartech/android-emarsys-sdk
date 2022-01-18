package com.emarsys.core.api

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
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

    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        uiHandler = Handler(Looper.getMainLooper())
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory(uiHandler).create()
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
    fun testInvoke_shouldHandlePrimitives_boolean() {
        val latch = CountDownLatch(1)
        val proxiedTestClass = (TestClassWithPrimitives() as Proxyable).proxyWithHandler(
            concurrentHandlerHolder,
            timeout = 1
        )
        var error: Exception? = null
        concurrentHandlerHolder.coreHandler.post {
            try {
                val result = proxiedTestClass.testBoolean()
                result shouldBe false
            } catch (e: Exception) {
                error = e
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        error shouldBe null
    }

    @Test
    fun testInvoke_shouldHandlePrimitives_double() {
        val latch = CountDownLatch(1)
        val proxiedTestClass = (TestClassWithPrimitives() as Proxyable).proxyWithHandler(
            concurrentHandlerHolder,
            timeout = 1
        )
        var error: Exception? = null
        concurrentHandlerHolder.coreHandler.post {
            try {
                val result = proxiedTestClass.testDouble()
                result shouldBe 0.0
            } catch (e: Exception) {
                error = e
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        error shouldBe null
    }

    @Test
    fun testInvoke_shouldHandlePrimitives_char() {
        val latch = CountDownLatch(1)
        val proxiedTestClass = (TestClassWithPrimitives() as Proxyable).proxyWithHandler(
            concurrentHandlerHolder,
            timeout = 1
        )
        var error: Exception? = null
        concurrentHandlerHolder.coreHandler.post {
            try {
                val result = proxiedTestClass.testChar()
                result shouldBe Char(0)
            } catch (e: Exception) {
                error = e
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        error shouldBe null
    }
}

interface Proxyable {
    fun testBoolean(): Boolean
    fun testDouble(): Double
    fun testChar(): Char
}

class TestClassWithPrimitives : Proxyable {
    override fun testBoolean(): Boolean {
        return true
    }

    override fun testDouble(): Double {
        return 1.0
    }

    override fun testChar(): Char {
        return Char(123)
    }
}