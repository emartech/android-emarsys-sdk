package com.emarsys.core.app

import com.emarsys.core.session.Session
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class AppLifecycleObserverTest {
    private lateinit var mockSession: Session

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockSession = mock()
    }

    @Test
    fun onEnterForeground_sessionStart_shouldBeCalled() {
        AppLifecycleObserver(mockSession).onEnterForeground()

        verify(mockSession).startSession()
    }

    @Test
    fun onEnterBackground_endSession_shouldBeCalled() {
        AppLifecycleObserver(mockSession).onEnterBackground()

        verify(mockSession).endSession()
    }
}