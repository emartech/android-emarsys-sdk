package com.emarsys.mobileengage

import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.fake.FakeRestClient
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class MobileEngageRefreshTokenInternalTest {
    companion object {
        const val REQUEST_ID = "requestId"
    }

    private lateinit var refreshTokenInternal: RefreshTokenInternal
    private lateinit var mockRestClient: RestClient
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockResponseHandler: MobileEngageTokenResponseHandler
    private lateinit var mockCompletionListener: CompletionListener

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRestClient = mock()
        mockResponseHandler = mock()
        mockCompletionListener = mock()
        mockRequestModel = mock {
            on { id } doReturn REQUEST_ID
        }
        mockRequestModelFactory = mock {
            on { createRefreshContactTokenRequest() } doReturn (mockRequestModel)
        }

        refreshTokenInternal = MobileEngageRefreshTokenInternal(
            mockResponseHandler,
            mockRestClient,
            mockRequestModelFactory
        )
    }

    @Test
    fun testRefreshContactToken_shouldCallSubmitNow() {
        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        verify(mockRestClient).execute(eq(mockRequestModel), any())
    }

    @Test
    fun testRefreshContactToken_shouldProcessResponseHandler_andCallCompletionListener_whenSuccess() {
        val mockResponseModel: ResponseModel = mock {
            on { requestModel } doReturn (mockRequestModel)
        }
        val fakeRestClient = FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS)
        val latch = CountDownLatch(1)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(
            mockResponseHandler,
            fakeRestClient,
            mockRequestModelFactory
        )

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
        val mockResponseModel: ResponseModel = mock {
            on { requestModel } doReturn (mockRequestModel)
        }

        val fakeRestClient =
            FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)
        val mockCompletionListener: CompletionListener = mock()
        val latch = CountDownLatch(1)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(
            mockResponseHandler,
            fakeRestClient,
            mockRequestModelFactory
        )

        refreshTokenInternal.refreshContactToken(mockCompletionListener)
        refreshTokenInternal.refreshContactToken {
            latch.countDown()
        }

        latch.await()
        verify(mockCompletionListener).onCompleted(any<ResponseErrorException>())
    }

    @Test
    fun testRefreshContactToken_shouldCallCompletionListener_whenException() {
        val fakeRestClient = FakeRestClient(Exception())
        val mockCompletionListener: CompletionListener = mock()
        val latch = CountDownLatch(1)
        refreshTokenInternal = MobileEngageRefreshTokenInternal(
            mockResponseHandler,
            fakeRestClient,
            mockRequestModelFactory
        )

        refreshTokenInternal.refreshContactToken(mockCompletionListener)
        refreshTokenInternal.refreshContactToken {
            latch.countDown()
        }

        latch.await()
        verify(mockCompletionListener).onCompleted(any<Exception>())
    }

    @Test
    fun testRefreshContactToken_shouldCallCompletionListener_whenRequestModelFactoryThrowsIllegalArgumentException() {
        val mockCompletionListener: CompletionListener = mock()
        whenever(mockRequestModelFactory.createRefreshContactTokenRequest()).thenThrow(
            IllegalArgumentException("")
        )

        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        verify(mockCompletionListener).onCompleted(any<Exception>())
    }
}