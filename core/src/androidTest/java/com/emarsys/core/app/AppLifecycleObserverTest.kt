package com.emarsys.core.app

import android.os.HandlerThread
import com.emarsys.core.concurrency.CoreSdkHandler
import com.emarsys.core.session.Session
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*
import java.util.concurrent.CountDownLatch

class AppLifecycleObserverTest {
    private lateinit var mockSession: Session
    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private lateinit var coreSdkHandler: CoreSdkHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockSession = mock()
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        coreSdkHandler = CoreSdkHandler(handlerThread)
        appLifecycleObserver = AppLifecycleObserver(mockSession, coreSdkHandler)
    }

    @Test
    fun onEnterForeground_sessionStart_shouldBeCalled() {
        val latch = CountDownLatch(1)

        appLifecycleObserver.onEnterForeground()
        coreSdkHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).startSession()
    }

    @Test
    fun onEnterBackground_endSession_shouldBeCalled() {
        val latch = CountDownLatch(1)

        appLifecycleObserver.onEnterBackground()
        coreSdkHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).endSession()
    }

    @Test
    fun testStartSession_startsSessionOnCoreSdkThread() {
       val threadSpy = ThreadSpy<Unit>()
        org.mockito.Mockito.doAnswer(threadSpy).`when`(mockSession).startSession()

        appLifecycleObserver.onEnterForeground()

        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testEndSession_endsSessionOnCoreSdkThread() {
        val threadSpy = ThreadSpy<Unit>()

        org.mockito.Mockito.doAnswer(threadSpy).`when`(mockSession).endSession()

        appLifecycleObserver.onEnterBackground()
        threadSpy.verifyCalledOnCoreSdkThread()
    }
}