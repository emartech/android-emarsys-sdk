package com.emarsys.mobileengage.inbox

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.worker.DelegatorCompletionHandlerProvider
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.mobileengage.fake.FakeRestClient
import com.emarsys.mobileengage.fake.FakeResultListener
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class DefaultMessageInboxInternalTest  {
    private companion object {
        private const val TAG = "READ"
        private const val LOWER_CASED_TAG = "read"
        private const val MESSAGE_ID = Integer.MAX_VALUE.toString()
        private const val ADD_EVENT_NAME = "inbox:tag:add"
        private const val REMOVE_EVENT_NAME = "inbox:tag:remove"
    }

    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockMessageInboxResponseMapper: MessageInboxResponseMapper
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var messageInboxInternal: DefaultMessageInboxInternal
    private lateinit var latch: CountDownLatch
    private lateinit var mockScope: CoroutineScope
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var messages: List<Message>
    private lateinit var message: Message


    @Before
    fun setUp() {
        message = Message(
            "testMessageId",
            "testCampaignId",
            collapseId = "testCollapseId",
            title = "testTitle",
            body = "testBody",
            imageUrl = null,
            imageAltText = null,
            receivedAt = 12345,
            updatedAt = null,
            expiresAt = null,
            tags = listOf("testtag1", "testtag2"),
            properties = null,
            actions = null
        )
        messages = listOf(message)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockScope = mock()
        latch = CountDownLatch(1)
        mockMessageInboxResponseMapper = mock {
            on { map(any()) } doReturn InboxResult(messages)
        }
        mockRequestManager = mock()
        mockRequestModel = mock {
            on { id } doReturn "requestId"
        }
        mockRequestModelFactory = mock {
            on { createFetchInboxMessagesRequest() } doReturn mockRequestModel
            on { createInternalCustomEventRequest(any(), any()) } doReturn mockRequestModel
        }

        messageInboxInternal = DefaultMessageInboxInternal(
            concurrentHandlerHolder,
            mockRequestManager,
            mockRequestModelFactory,
            mockMessageInboxResponseMapper
        )
    }

    @Test
    fun testFetchInboxMessages_callsRequestModelFactoryForCreateFetchInboxMessagesRequest_andSubmitsToRequestManager() {
        val mockResultListener = mock<ResultListener<Try<InboxResult>>>()
        messageInboxInternal.fetchMessages(mockResultListener)

        verify(mockRequestModelFactory).createFetchInboxMessagesRequest()
        verify(mockRequestManager).submitNow(eq(mockRequestModel), any(), any())
    }

    @Test
    fun testFetchInboxMessages_resultListener_onSuccess() {
        val mockResponse: ResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
            on { statusCode } doReturn 200
            on { message } doReturn "OK"
        }
        val messageInboxInternal = DefaultMessageInboxInternal(
            concurrentHandlerHolder,
            requestManagerWithRestClient(
                FakeRestClient(
                    mockResponse,
                    FakeRestClient.Mode.SUCCESS,
                    concurrentHandlerHolder.coreHandler
                )
            ),
            mockRequestModelFactory,
            mockMessageInboxResponseMapper
        )

        val fakeResultListener = FakeResultListener<InboxResult>(latch)

        messageInboxInternal.fetchMessages(fakeResultListener)
        fakeResultListener.latch.await()

        fakeResultListener.successCount shouldBe 1
        verify(mockMessageInboxResponseMapper).map(mockResponse)
    }

    @Test
    fun testFetchInboxMessages_shouldStoreMessages() {
        val mockResponse: ResponseModel = mock()
        whenever(mockRequestManager.submitNow(any(), any(), any())).doAnswer { invocation ->
            invocation.arguments[1]?.let {
                (it as CoreCompletionHandler).onSuccess("testId", mockResponse)
            }
        }

        messageInboxInternal.messages = null

        messageInboxInternal.fetchMessages({
            latch.countDown()
        })
        latch.await()

        messageInboxInternal.messages shouldBe messages
    }

    @Test
    fun testFetchInboxMessages_shouldClearMessages_withResponseError() {
        val mockResponse: ResponseModel = mock()
        whenever(mockRequestManager.submitNow(any(), any(), any())).doAnswer { invocation ->
            invocation.arguments[1]?.let {
                (it as CoreCompletionHandler).onError("testId", mockResponse)
            }
        }

        messageInboxInternal.messages = messages

        messageInboxInternal.fetchMessages({
            latch.countDown()
        })
        latch.await()

        messageInboxInternal.messages shouldBe null
    }

    @Test
    fun testFetchInboxMessages_shouldClearMessages_withExceptionError() {
        whenever(mockRequestManager.submitNow(any(), any(), any())).doAnswer { invocation ->
            invocation.arguments[1]?.let {
                (it as CoreCompletionHandler).onError(
                    "testId",
                    java.lang.Exception("testException")
                )
            }
        }

        messageInboxInternal.messages = messages

        messageInboxInternal.fetchMessages({
            latch.countDown()
        })
        latch.await()

        messageInboxInternal.messages shouldBe null
    }

    @Test
    fun testFetchInboxMessages_resultListener_onErrorWithResponseModel() {
        val errorResponse: ResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
            on { statusCode } doReturn 500
            on { message } doReturn "error"
            on { body } doReturn "Error happened"
        }
        val messageInboxInternal = DefaultMessageInboxInternal(
            concurrentHandlerHolder,
            requestManagerWithRestClient(
                FakeRestClient(
                    errorResponse,
                    FakeRestClient.Mode.ERROR_RESPONSE_MODEL,
                    concurrentHandlerHolder.coreHandler
                )
            ),
            mockRequestModelFactory,
            mockMessageInboxResponseMapper
        )

        val fakeResultListener = FakeResultListener<InboxResult>(latch)

        messageInboxInternal.fetchMessages(fakeResultListener)
        fakeResultListener.latch.await()

        val expectedException = ResponseErrorException(
            errorResponse.statusCode,
            errorResponse.message,
            errorResponse.body
        )

        fakeResultListener.successCount shouldBe 0
        fakeResultListener.errorCount shouldBe 1
        fakeResultListener.errorCause shouldBe expectedException
    }

    @Test
    fun testFetchInboxMessages_resultListener_onErrorWithException() {
        val expectedException = Exception("TestException")

        val messageInboxInternal = DefaultMessageInboxInternal(
            concurrentHandlerHolder,
            requestManagerWithRestClient(
                FakeRestClient(
                    expectedException,
                    concurrentHandlerHolder.coreHandler
                )
            ),
            mockRequestModelFactory,
            mockMessageInboxResponseMapper
        )

        val fakeResultListener = FakeResultListener<InboxResult>(latch)

        messageInboxInternal.fetchMessages(fakeResultListener)
        fakeResultListener.latch.await()

        fakeResultListener.successCount shouldBe 0
        fakeResultListener.errorCount shouldBe 1
        fakeResultListener.errorCause shouldBe expectedException
    }

    @Test
    fun testTrackAddTag_callsRequestModelFactoryForInternalCustomEventRequest_andSubmitsToRequestManager() {
        val mockCompletionListener: CompletionListener = mock()
        val eventAttributes = mapOf(
            "messageId" to MESSAGE_ID,
            "tag" to LOWER_CASED_TAG
        )
        whenever(
            mockRequestModelFactory.createInternalCustomEventRequest(
                ADD_EVENT_NAME,
                eventAttributes
            )
        ).thenReturn(mockRequestModel)

        messageInboxInternal.addTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(mockRequestModelFactory).createInternalCustomEventRequest(
            ADD_EVENT_NAME,
            eventAttributes
        )
        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun addTag_shouldSendRequest_when_messageAvailable_tagNotAvailable() {
        whenever(mockRequestManager.submit(any(), any())).doAnswer { invocation ->
            invocation.arguments[1]?.let {
                (it as CompletionListener).onCompleted(null)
            }
        }

        val completionListener = CompletionListener {
            latch.countDown()
        }

        messageInboxInternal.messages = messages

        messageInboxInternal.addTag(
            "testTag3",
            "testMessageId",
            completionListener
        )

        latch.await()

        verify(
            mockRequestModelFactory
        ).createInternalCustomEventRequest(
            "inbox:tag:add", mapOf(
                "messageId" to "testMessageId",
                "tag" to "testtag3"
            )
        )
        verify(
            mockRequestManager
        ).submit(
            mockRequestModel,
            completionListener
        )
    }

    @Test
    fun addTag_shouldNotSendRequest_when_messageAvailable_tagAvailable() {
        messageInboxInternal.messages = messages

        val completionListener = CompletionListener {
            latch.countDown()
        }

        messageInboxInternal.addTag(
            "testTag2",
            "testMessageId",
            completionListener
        )

        latch.await()

        verifyNoInteractions(mockRequestModelFactory)
        verifyNoInteractions(mockRequestManager)
    }

    @Test
    fun addTag_shouldNotSendRequest_when_messageNotAvailable_tagNotAvailable() {
        messageInboxInternal.messages = messages

        val completionListener = CompletionListener {
            latch.countDown()
        }

        messageInboxInternal.addTag(
            "testTag3",
            "notTestMessageId",
            completionListener
        )

        latch.await()

        verifyNoInteractions(mockRequestModelFactory)
        verifyNoInteractions(mockRequestManager)
    }

    @Test
    fun removeTag_shouldSendRequest_when_messageAvailable_tagAvailable() {
        whenever(mockRequestManager.submit(any(), any())).doAnswer { invocation ->
            invocation.arguments[1]?.let {
                (it as CompletionListener).onCompleted(Exception("testException"))
            }
        }

        val completionListener = CompletionListener {
            latch.countDown()
        }

        messageInboxInternal.messages = messages

        messageInboxInternal.removeTag(
            "testTag2",
            "testMessageId",
            completionListener
        )

        latch.await()

        verify(
            mockRequestModelFactory
        ).createInternalCustomEventRequest(
            "inbox:tag:remove", mapOf(
                "messageId" to "testMessageId",
                "tag" to "testtag2"
            )
        )
        verify(
            mockRequestManager
        ).submit(
            mockRequestModel,
            completionListener
        )
    }

    @Test
    fun removeTag_shouldNotSendRequest_when_messageAvailable_tagNotAvailable() {
        messageInboxInternal.messages = messages

        val completionListener = CompletionListener {
            latch.countDown()
        }

        messageInboxInternal.removeTag(
            "testTag3",
            "testMessageId",
            completionListener
        )

        latch.await()

        verifyNoInteractions(mockRequestModelFactory)
        verifyNoInteractions(mockRequestManager)
    }

    @Test
    fun addTag_shouldNotSendRequest_when_messageNotAvailable_tagAvailable() {
        messageInboxInternal.messages = messages

        val completionListener = CompletionListener {
            latch.countDown()
        }

        messageInboxInternal.removeTag(
            "testTag2",
            "notTestMessageId",
            completionListener
        )

        latch.await()

        verifyNoInteractions(mockRequestModelFactory)
        verifyNoInteractions(mockRequestManager)
    }

    @Test
    fun testRemoveTag_callsRequestModelFactoryForInternalCustomEventRequest_andSubmitsToRequestManager() {
        val mockCompletionListener: CompletionListener = mock()
        val eventAttributes = mapOf(
            "messageId" to MESSAGE_ID,
            "tag" to LOWER_CASED_TAG
        )
        whenever(
            mockRequestModelFactory.createInternalCustomEventRequest(
                REMOVE_EVENT_NAME,
                eventAttributes
            )
        ).thenReturn(mockRequestModel)

        messageInboxInternal.removeTag(TAG, MESSAGE_ID, mockCompletionListener)

        verify(mockRequestModelFactory).createInternalCustomEventRequest(
            REMOVE_EVENT_NAME,
            eventAttributes
        )
        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Suppress("UNCHECKED_CAST")
    private fun requestManagerWithRestClient(restClient: RestClient): RequestManager {
        val mockDelegatorCompletionHandlerProvider =
            mock<DelegatorCompletionHandlerProvider> {
                on { provide(any(), any()) } doAnswer {
                    it.arguments[1] as CoreCompletionHandler
                }
            }
        val mockProvider: CompletionHandlerProxyProvider = mock {
            on { provideProxy(isNull(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
        }
        return RequestManager(
            concurrentHandlerHolder,
            mock(),
            mock(),
            mock(),
            restClient,
            mock(),
            mock(),
            mockProvider,
            mockDelegatorCompletionHandlerProvider
        )
    }
}