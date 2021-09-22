package com.emarsys.core.app

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.emarsys.core.concurrency.CoreHandler
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.session.Session
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
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
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        uiHandler = Handler(Looper.getMainLooper())
        mockSession = mock()
        mockLifecycleOwner = mock()
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        coreHandler = CoreSdkHandler(CoreHandler(handlerThread))
        appLifecycleObserver = AppLifecycleObserver(mockSession, coreHandler)
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
        coreHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).startSession()
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
        coreHandler.post {
            latch.countDown()
        }

        latch.await()

        verify(mockSession).endSession()
    }
}