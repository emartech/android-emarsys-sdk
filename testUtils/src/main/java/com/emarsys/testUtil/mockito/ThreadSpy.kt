package com.emarsys.testUtil.mockito

import android.os.Looper
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers.startsWith
import org.junit.Assert.assertThat
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.concurrent.CountDownLatch

class ThreadSpy<T> @JvmOverloads constructor(private val result: T? = null) : Answer<T> {

    private val latch: CountDownLatch = CountDownLatch(1)

    private var thread: Thread? = null
    get() {
        latch.await()
        return field
    }

    override fun answer(invocation: InvocationOnMock): T? {
        thread = Thread.currentThread()
        latch.countDown()
        return result
    }

    fun verifyCalledOnMainThread() {
        val expected = Looper.getMainLooper().thread
        assertEquals(expected, thread)
    }

    fun verifyCalledOnCoreSdkThread() {
        assertThat(thread!!.name, startsWith("CoreSDKHandlerThread"))
    }
}
