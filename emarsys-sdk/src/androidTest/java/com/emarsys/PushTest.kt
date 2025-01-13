package com.emarsys

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.push.Push
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.IntegrationTestUtils
import io.mockk.mockk
import io.mockk.verify

class PushTest : AnnotationSpec() {
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockEventHandler: EventHandler
    private lateinit var mockNotificationInformationListener: NotificationInformationListener
    private lateinit var push: Push


    @Before
    fun setUp() {
        mockPushInternal = mockk(relaxed = true)
        mockCompletionListener = mockk(relaxed = true)
        mockEventHandler = mockk(relaxed = true)
        mockNotificationInformationListener = mockk(relaxed = true)
        val dependencyContainer = FakeDependencyContainer(pushInternal = mockPushInternal)

        setupEmarsysComponent(dependencyContainer)

        push = Push()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testPush_setPushToken_delegatesTo_mobileEngageInternal() {
        val pushToken = "PushToken"
        push.setPushToken(pushToken)
        verify { mockPushInternal.setPushToken(pushToken, null) }
    }

    @Test
    fun testPush_setPushToken_completionListener_delegatesTo_mobileEngageInternal() {
        val pushToken = "PushToken"
        push.setPushToken(pushToken, mockCompletionListener)
        verify { mockPushInternal.setPushToken(pushToken, mockCompletionListener) }
    }

    @Test
    fun testPush_getPushToken_delegatesTo_pushInternal() {
       push.pushToken
        verify { mockPushInternal.pushToken }
    }

    @Test
    fun testPush_removePushToken_delegatesTo_mobileEngageInternal() {
        push.clearPushToken()
        verify { mockPushInternal.clearPushToken(null) }
    }

    @Test
    fun testPush_removePushTokenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        push.clearPushToken(mockCompletionListener)
        verify { mockPushInternal.clearPushToken(mockCompletionListener) }
    }

    @Test
    fun testPush_setNotificationEventHandler_delegatesTo_pushInternal() {
        push.setNotificationEventHandler(mockEventHandler)
        verify { mockPushInternal.setNotificationEventHandler(mockEventHandler) }
    }

    @Test
    fun testPush_setSilentMessageEventHandler_delegatesTo_pushInternal() {
        push.setSilentMessageEventHandler(mockEventHandler)
        verify { mockPushInternal.setSilentMessageEventHandler(mockEventHandler) }
    }

    @Test
    fun testPush_setNotificationInformationListener_delegatesTo_pushInternal() {
        push.setNotificationInformationListener(mockNotificationInformationListener)
        verify { mockPushInternal.setNotificationInformationListener(mockNotificationInformationListener) }
    }

    @Test
    fun testPush_setSilentNotificationInformationListener_delegatesTo_pushInternal() {
        push.setSilentNotificationInformationListener(mockNotificationInformationListener)
        verify { mockPushInternal.setSilentNotificationInformationListener(mockNotificationInformationListener) }
    }
}