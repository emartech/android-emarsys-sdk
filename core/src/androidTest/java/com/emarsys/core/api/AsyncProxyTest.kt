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

    @Test
    fun testInvoke_shouldHandlePrimitives_boolean() {
        val latch = CountDownLatch(1)
        val proxiedTestClass = (TestClassWithPrimitives() as Testable).proxyWithHandler(handler, timeout = 1)
        var error: Exception? = null
        handler.post {
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
        val proxiedTestClass = (TestClassWithPrimitives() as Testable).proxyWithHandler(handler, timeout = 1)
        var error: Exception? = null
        handler.post {
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
        val proxiedTestClass = (TestClassWithPrimitives() as Testable).proxyWithHandler(handler, timeout = 1)
        var error: Exception? = null
        handler.post {
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

interface Testable {
    fun testBoolean(): Boolean
    fun testDouble(): Double
    fun testChar(): Char
}

class TestClassWithPrimitives : Testable {
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