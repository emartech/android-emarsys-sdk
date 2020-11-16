package com.emarsys.inbox

import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class MessageInboxTest {
    private companion object {
        private const val TAG = "READ"
        private const val MESSAGE_ID = Integer.MAX_VALUE.toString()
    }

    private lateinit var inbox: MessageInboxApi
    private lateinit var mockInboxInternal: MessageInboxInternal

    @Before
    fun setUp() {
        mockInboxInternal = mock()
        val dependencyContainer = FakeDependencyContainer(messageInboxInternal = mockInboxInternal)

        DependencyInjection.setup(dependencyContainer)
        inbox = MessageInbox()
    }

    @After
    fun tearDown() {
        try {
            val handler = getDependency<Handler>("coreSdkHandler")
            val looper: Looper? = handler.looper
            looper?.quit()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testFetchInboxMessagesDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener = mock<ResultListener<Try<InboxResult>>>()
        inbox.fetchMessages(resultListener)

        verify(mockInboxInternal).fetchMessages(resultListener)
    }

    @Test
    fun testFetchInboxMessagesWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener: (Try<InboxResult>) -> Unit = {}

        inbox.fetchMessages(resultListener)

        verify(mockInboxInternal).fetchMessages(any())
    }

    @Test
    fun testTrackAddTagDelegatesToInternalMethod_throughRunnerProxy() {
        inbox.addTag(TAG, MESSAGE_ID)

        verify(mockInboxInternal).addTag(TAG, MESSAGE_ID, null)
    }

    @Test
    fun testTrackAddTagWithCompletionListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: CompletionListener = mock()
        inbox.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(mockInboxInternal).addTag(TAG, MESSAGE_ID, mockCompletionListener)
    }

    @Test
    fun testTrackAddTagWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inbox.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(mockInboxInternal).addTag(eq(TAG), eq(MESSAGE_ID), any())
    }

    @Test
    fun testTrackRemoveTagDelegatesToInternalMethod_throughRunnerProxy() {
        inbox.removeTag(TAG, MESSAGE_ID)

        verify(mockInboxInternal).removeTag(TAG, MESSAGE_ID, null)
    }

    @Test
    fun testTrackRemoveTagWithCompletionListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: CompletionListener = mock()
        inbox.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(mockInboxInternal).removeTag(TAG, MESSAGE_ID, mockCompletionListener)
    }

    @Test
    fun testTrackRemoveTagWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inbox.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(mockInboxInternal).removeTag(eq(TAG), eq(MESSAGE_ID), any())
    }
}