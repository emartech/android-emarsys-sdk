package com.emarsys.inbox

import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxMessage
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

class MessageInboxProxyTest {
    private lateinit var inboxProxy: MessageInboxApi
    private lateinit var mockInboxInternal: MessageInboxInternal
    private lateinit var spyRunnerProxy: RunnerProxy

    @Before
    fun setUp() {
        mockInboxInternal = mock()
        spyRunnerProxy = spy()
        inboxProxy = MessageInboxProxy(spyRunnerProxy, mockInboxInternal)
    }

    @Test
    fun testFetchNotificationsDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener = mock<ResultListener<Try<MessageInboxResult>>>()
        inboxProxy.fetchNotifications(resultListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).fetchNotifications(resultListener)
    }

    @Test
    fun testFetchNotificationsWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener: (Try<MessageInboxResult>) -> Unit = {}

        inboxProxy.fetchNotifications(resultListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).fetchNotifications(any())
    }

    @Test
    fun testTrackNotificationOpenDelegatesToInternalMethod_throughRunnerProxy() {
        val inboxMessage = InboxMessage("test")
        inboxProxy.trackNotificationOpen(inboxMessage)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).trackNotificationOpen(inboxMessage, null)
    }

    @Test
    fun testTrackNotificationOpenWithResultListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val inboxMessage = InboxMessage("test")
        val mockCompletionListener = mock<CompletionListener>()
        inboxProxy.trackNotificationOpen(inboxMessage, mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).trackNotificationOpen(inboxMessage, mockCompletionListener)
    }

    @Test
    fun testTrackNotificationOpenWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val inboxMessage = InboxMessage("test")
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inboxProxy.trackNotificationOpen(inboxMessage, mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).trackNotificationOpen(eq(inboxMessage), any())
    }
}