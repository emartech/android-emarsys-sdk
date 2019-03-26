package com.emarsys.mobileengage

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.fake.FakeRequestManager
import com.emarsys.mobileengage.request.RequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*

class MobileEngageRefreshTokenInternalTest {
    private lateinit var refreshTokenInternal: RefreshTokenInternal
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRequestModelFactory: RequestModelFactory
    private lateinit var mockResponseHandler: MobileEngageTokenResponseHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRequestManager = mock(RequestManager::class.java)
        mockResponseHandler = mock(MobileEngageTokenResponseHandler::class.java)
        mockRequestModel = mock(RequestModel::class.java)
        mockRequestModelFactory = mock(RequestModelFactory::class.java).apply {
            whenever(createRefreshContactTokenRequest()).thenReturn(mockRequestModel)
        }

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, mockRequestManager, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_tokenResponseHandler_mustNotBeNull() {
        MobileEngageRefreshTokenInternal(null, mockRequestManager, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        MobileEngageRefreshTokenInternal(mockResponseHandler, null, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        MobileEngageRefreshTokenInternal(mockResponseHandler, mockRequestManager, null)
    }

    @Test
    fun testRefreshContactToken_shouldCallSubmitNow() {
        val mockCompletionListener = mock(CompletionListener::class.java)
        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any(CoreCompletionHandler::class.java))
    }

    @Test
    fun testRefreshContactToken_shouldProcessResponseHandler_andCallCompletionListener_whenSuccess() {
        val mockResponseModel = mock(ResponseModel::class.java)
        val fakeRequestManager = FakeRequestManager(FakeRequestManager.ResponseType.SUCCESS, mockResponseModel)
        val mockCompletionListener = mock(CompletionListener::class.java)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, fakeRequestManager, mockRequestModelFactory)

        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        val inOrder = inOrder(mockResponseHandler, mockCompletionListener)

        inOrder.verify(mockResponseHandler).processResponse(mockResponseModel)
        inOrder.verify(mockCompletionListener).onCompleted(null)

    }

    @Test
    fun testRefreshContactToken_shouldCallCompletionListener_whenFailure() {
        val mockResponseModel = mock(ResponseModel::class.java)
        val fakeRequestManager = FakeRequestManager(FakeRequestManager.ResponseType.FAILURE, mockResponseModel)
        val mockCompletionListener = mock(CompletionListener::class.java)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, fakeRequestManager, mockRequestModelFactory)

        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        verify(mockCompletionListener).onCompleted(any(ResponseErrorException::class.java))
    }

    @Test
    fun testRefreshContactToken_shouldCallCompletionListener_whenException() {
        val mockResponseModel = mock(ResponseModel::class.java)
        val fakeRequestManager = FakeRequestManager(FakeRequestManager.ResponseType.EXCEPTION, mockResponseModel)
        val mockCompletionListener = mock(CompletionListener::class.java)

        refreshTokenInternal = MobileEngageRefreshTokenInternal(mockResponseHandler, fakeRequestManager, mockRequestModelFactory)

        refreshTokenInternal.refreshContactToken(mockCompletionListener)

        verify(mockCompletionListener).onCompleted(any(Exception::class.java))
    }
}