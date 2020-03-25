package com.emarsys.inbox

import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
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
}