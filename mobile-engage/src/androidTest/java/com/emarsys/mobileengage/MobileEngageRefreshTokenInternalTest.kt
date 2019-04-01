package com.emarsys.mobileengage

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.fake.FakeRestClient
import com.emarsys.mobileengage.request.RequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch

class MobileEngageRefreshTokenInternalTest {
    companion object {
        const val REQUEST_ID = "requestId"
    }

    private lateinit var refreshTokenInternal: RefreshTokenInternal
    private lateinit var mockRestClient: RestClient
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRequestModelFactory: RequestModelFactory
    private lateinit var mockResponseHandler: MobileEngageTokenResponseHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRestClient = mock(RestClient::class.java)
        mockResponseHandler = mock(MobileEngageTokenResponseHandler::class.java)
        mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(id).thenReturn(REQUEST_ID)
        }
        mockRequestModelFactory = mock(RequestModelFactory::class.java).apply {
            whenever(createRefreshContactTokenRequest()).thenReturn(mockRequestModel)
        }

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, mockRestClient, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_tokenResponseHandler_mustNotBeNull() {
        MobileEngageRefreshTokenInternal(null, mockRestClient, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        MobileEngageRefreshTokenInternal(mockResponseHandler, null, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        MobileEngageRefreshTokenInternal(mockResponseHandler, mockRestClient, null)
    }

    @Test
    fun testRefreshContactToken_shouldCallSubmitNow() {
        val mockCompletionListener = mock(CompletionListener::class.java)
        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        verify(mockRestClient).execute(eq(mockRequestModel), any(CoreCompletionHandler::class.java))
    }

    @Test
    fun testRefreshContactToken_shouldProcessResponseHandler_andCallCompletionListener_whenSuccess() {
        val mockResponseModel = mock(ResponseModel::class.java).apply {
            whenever(requestModel).thenReturn(mockRequestModel)
        }
        val fakeRestClient = FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS)
        val mockCompletionListener = mock(CompletionListener::class.java)
        val latch = CountDownLatch(1)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, fakeRestClient, mockRequestModelFactory)

        refreshTokenInternal.refreshContactToken(mockCompletionListener)
        refreshTokenInternal.refreshContactToken {
            latch.countDown()
        }

        latch.await()
        val inOrder = inOrder(mockResponseHandler, mockCompletionListener)

        inOrder.verify(mockResponseHandler).processResponse(mockResponseModel)
        inOrder.verify(mockCompletionListener).onCompleted(null)

    }

    @Test
    fun testRefreshContactToken_shouldCallCompletionListener_whenFailure() {
        val mockResponseModel = mock(ResponseModel::class.java).apply {
            whenever(requestModel).thenReturn(mockRequestModel)
        }

        val fakeRestClient = FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)
        val mockCompletionListener = mock(CompletionListener::class.java)
        val latch = CountDownLatch(1)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, fakeRestClient, mockRequestModelFactory)

        refreshTokenInternal.refreshContactToken(mockCompletionListener)
        refreshTokenInternal.refreshContactToken {
            latch.countDown()
        }

        latch.await()
        verify(mockCompletionListener).onCompleted(any(ResponseErrorException::class.java))
    }

    @Test
    fun testRefreshContactToken_shouldCallCompletionListener_whenException() {
        val fakeRestClient = FakeRestClient(Exception())
        val mockCompletionListener = mock(CompletionListener::class.java)
        val latch = CountDownLatch(1)
        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, fakeRestClient, mockRequestModelFactory)

        refreshTokenInternal.refreshContactToken(mockCompletionListener)
        refreshTokenInternal.refreshContactToken {
            latch.countDown()
        }

        latch.await()
        verify(mockCompletionListener).onCompleted(any(Exception::class.java))
    }
}