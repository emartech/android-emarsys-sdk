package com.emarsys.core.app

import android.os.HandlerThread
import com.emarsys.core.concurrency.CoreHandler
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.session.Session
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.*
import java.util.concurrent.CountDownLatch

class AppLifecycleObserverTest {
    private lateinit var mockSession: Session
    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private lateinit var coreHandler: CoreSdkHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockSession = mock()
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        coreHandler = CoreSdkHandler(CoreHandler(handlerThread))
        appLifecycleObserver = AppLifecycleObserver(mockSession, coreHandler)
    }

    @Test
    fun onEnterForeground_sessionStart_shouldBeCalled() {
        val latch = CountDownLatch(1)

        appLifecycleObserver.onEnterForeground()
        coreHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).startSession()
    }

    @Test
    fun onEnterBackground_endSession_shouldBeCalled() {
        val latch = CountDownLatch(1)

        appLifecycleObserver.onEnterBackground()
        coreHandler.post {
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