package com.emarsys.core.app

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.session.Session
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class AppLifecycleObserverTest : AnnotationSpec() {
    private lateinit var mockSession: Session
    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private lateinit var coreHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var uiHandler: Handler


    @Before
    fun setUp() {
        uiHandler = Handler(Looper.getMainLooper())
        mockSession = mock()
        mockLifecycleOwner = mock()
        coreHandlerHolder = ConcurrentHandlerHolderFactory.create()
        appLifecycleObserver = AppLifecycleObserver(mockSession, coreHandlerHolder)
    }

    @Test
    fun onEnterForeground_sessionStart_shouldBeCalled() {
        val latch = CountDownLatch(1)
        uiHandler.post {
            val lifecycle = LifecycleRegistry(mockLifecycleOwner)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)

            whenever(mockLifecycleOwner.lifecycle).thenReturn(lifecycle)
        }

        appLifecycleObserver.onStart(mockLifecycleOwner)
        coreHandlerHolder.coreHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).startSession(any())
    }

    @Test
    fun onEnterBackground_endSession_shouldBeCalled() {
        val latch = CountDownLatch(1)

        uiHandler.post {
            val lifecycle = LifecycleRegistry(mockLifecycleOwner)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

            whenever(mockLifecycleOwner.lifecycle).thenReturn(lifecycle)
        }

        appLifecycleObserver.onStop(mockLifecycleOwner)
        coreHandlerHolder.coreHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).endSession(any())
    }
}