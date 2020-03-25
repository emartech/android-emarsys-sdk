package com.emarsys.inbox

import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

class MessageInboxProxyTest {
    private companion object {
        private const val TAG = "READ"
        private const val MESSAGE_ID = "testMessageId"
    }

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
    fun testFetchInboxMessagesDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener = mock<ResultListener<Try<MessageInboxResult>>>()
        inboxProxy.fetchNotifications(resultListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).fetchInboxMessages(resultListener)
    }

    @Test
    fun testFetchInboxMessagesWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener: (Try<MessageInboxResult>) -> Unit = {}

        inboxProxy.fetchNotifications(resultListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).fetchInboxMessages(any())
    }

    @Test
    fun testTrackAddTagDelegatesToInternalMethod_throughRunnerProxy() {
        inboxProxy.addTag(TAG, MESSAGE_ID)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).addTag(TAG, MESSAGE_ID, null)
    }

    @Test
    fun testTrackAddTagWithCompletionListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: CompletionListener = mock()
        inboxProxy.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).addTag(TAG, MESSAGE_ID, mockCompletionListener)
    }

    @Test
    fun testTrackAddTagWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inboxProxy.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).addTag(eq(TAG), eq(MESSAGE_ID), any())
    }

    @Test
    fun testTrackRemoveTagDelegatesToInternalMethod_throughRunnerProxy() {
        inboxProxy.removeTag(TAG, MESSAGE_ID)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).removeTag(TAG, MESSAGE_ID, null)
    }

    @Test
    fun testTrackRemoveTagWithCompletionListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: CompletionListener = mock()
        inboxProxy.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).removeTag(TAG, MESSAGE_ID, mockCompletionListener)
    }

    @Test
    fun testTrackRemoveTagWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inboxProxy.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockInboxInternal).removeTag(eq(TAG), eq(MESSAGE_ID), any())
    }
}