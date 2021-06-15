package com.emarsys

import android.content.Intent
import com.emarsys.core.api.result.CompletionListener

import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.push.Push
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class PushTest {
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockEventHandler: EventHandler
    private lateinit var mockNotificationInformationListener: NotificationInformationListener
    private lateinit var push: Push

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockPushInternal = mock()
        mockCompletionListener = mock()
        mockEventHandler = mock()
        mockNotificationInformationListener = mock()
        val dependencyContainer = FakeDependencyContainer(pushInternal = mockPushInternal)

        setupEmarsysComponent(dependencyContainer)

        push = Push()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testPush_trackMessageOpen_delegatesTo_mobileEngageInternal() {
        val intent = Mockito.mock(Intent::class.java)
        push.trackMessageOpen(intent)
        Mockito.verify(mockPushInternal).trackMessageOpen(intent, null)
    }

    @Test
    fun testPush_trackMessageOpenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        val intent = Mockito.mock(Intent::class.java)
        push.trackMessageOpen(intent, mockCompletionListener)
        Mockito.verify(mockPushInternal).trackMessageOpen(intent, mockCompletionListener)
    }

    @Test
    fun testPush_setPushToken_delegatesTo_mobileEngageInternal() {
        val pushToken = "PushToken"
        push.setPushToken(pushToken)
        Mockito.verify(mockPushInternal).setPushToken(pushToken, null)
    }

    @Test
    fun testPush_setPushToken_completionListener_delegatesTo_mobileEngageInternal() {
        val pushToken = "PushToken"
        push.setPushToken(pushToken, mockCompletionListener)
        Mockito.verify(mockPushInternal).setPushToken(pushToken, mockCompletionListener)
    }

    @Test
    fun testPush_getPushToken_delegatesTo_pushInternal() {
       push.pushToken
        Mockito.verify(mockPushInternal).pushToken
    }

    @Test
    fun testPush_removePushToken_delegatesTo_mobileEngageInternal() {
        push.clearPushToken()
        Mockito.verify(mockPushInternal).clearPushToken(null)
    }

    @Test
    fun testPush_removePushTokenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        push.clearPushToken(mockCompletionListener)
        Mockito.verify(mockPushInternal).clearPushToken(mockCompletionListener)
    }

    @Test
    fun testPush_setNotificationEventHandler_delegatesTo_pushInternal() {
        push.setNotificationEventHandler(mockEventHandler)
        Mockito.verify(mockPushInternal).setNotificationEventHandler(mockEventHandler)
    }

    @Test
    fun testPush_setSilentMessageEventHandler_delegatesTo_pushInternal() {
        push.setSilentMessageEventHandler(mockEventHandler)
        Mockito.verify(mockPushInternal).setSilentMessageEventHandler(mockEventHandler)
    }

    @Test
    fun testPush_setNotificationInformationListener_delegatesTo_pushInternal() {
        push.setNotificationInformationListener(mockNotificationInformationListener)
        Mockito.verify(mockPushInternal).setNotificationInformationListener(mockNotificationInformationListener)
    }

    @Test
    fun testPush_setSilentNotificationInformationListener_delegatesTo_pushInternal() {
        push.setSilentNotificationInformationListener(mockNotificationInformationListener)
        Mockito.verify(mockPushInternal).setSilentNotificationInformationListener(mockNotificationInformationListener)
    }
}