package com.emarsys.inbox


import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.IntegrationTestUtils
import io.mockk.mockk
import io.mockk.verify

class MessageInboxTest : AnnotationSpec() {
    private companion object {
        private const val TAG = "READ"
        private const val MESSAGE_ID = Integer.MAX_VALUE.toString()
    }

    private lateinit var inbox: MessageInboxApi
    private lateinit var mockInboxInternal: MessageInboxInternal

    @Before
    fun setUp() {
        mockInboxInternal = mockk(relaxed = true)
        val dependencyContainer = FakeDependencyContainer(messageInboxInternal = mockInboxInternal)

        setupEmarsysComponent(dependencyContainer)
        inbox = MessageInbox()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testFetchInboxMessagesDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener: ResultListener<Try<InboxResult>> = mockk(relaxed = true)
        inbox.fetchMessages(resultListener)

        verify { mockInboxInternal.fetchMessages(resultListener) }
    }

    @Test
    fun testFetchInboxMessagesWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val resultListener: (Try<InboxResult>) -> Unit = {}

        inbox.fetchMessages(resultListener)

        verify { mockInboxInternal.fetchMessages(any()) }
    }

    @Test
    fun testTrackAddTagDelegatesToInternalMethod_throughRunnerProxy() {
        inbox.addTag(TAG, MESSAGE_ID)

        verify { mockInboxInternal.addTag(TAG, MESSAGE_ID, null) }
    }

    @Test
    fun testTrackAddTagWithCompletionListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: CompletionListener = mockk(relaxed = true)
        inbox.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify { mockInboxInternal.addTag(TAG, MESSAGE_ID, mockCompletionListener) }
    }

    @Test
    fun testTrackAddTagWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inbox.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify { mockInboxInternal.addTag(eq(TAG), eq(MESSAGE_ID), any()) }
    }

    @Test
    fun testTrackRemoveTagDelegatesToInternalMethod_throughRunnerProxy() {
        inbox.removeTag(TAG, MESSAGE_ID)

        verify { mockInboxInternal.removeTag(TAG, MESSAGE_ID, null) }
    }

    @Test
    fun testTrackRemoveTagWithCompletionListenerDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: CompletionListener = mockk(relaxed = true)
        inbox.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify { mockInboxInternal.removeTag(TAG, MESSAGE_ID, mockCompletionListener) }
    }

    @Test
    fun testTrackRemoveTagWithLambdaDelegatesToInternalMethod_throughRunnerProxy() {
        val mockCompletionListener: (Throwable?) -> Unit = {}
        inbox.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify { mockInboxInternal.removeTag(eq(TAG), eq(MESSAGE_ID), any()) }
    }
}