package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.net.URL

class ClientInfoResponseHandlerTest {

    companion object {
        val URL: URL = URL("https://me-client.eservice.emarsys.net/v3/apps/12341/client")
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var clientInfoResponseHandler: ClientInfoResponseHandler

    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockDeviceInfoHashStorage: Storage<Int>
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockDeviceInfo = mock(DeviceInfo::class.java)
        mockDeviceInfoHashStorage = mock(Storage::class.java) as Storage<Int>

        mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL)
        }
        mockResponseModel = mock(ResponseModel::class.java).apply {
            whenever(requestModel).thenReturn(mockRequestModel)
        }

        clientInfoResponseHandler = ClientInfoResponseHandler(mockDeviceInfo, mockDeviceInfoHashStorage)
    }

    @Test
    fun testShouldHandleResponse_true_whenRequestIsClientInfo() {
        val result = clientInfoResponseHandler.shouldHandleResponse(mockResponseModel)

        result shouldBe true
    }

    @Test
    fun testHandleResponse() {
        whenever(mockDeviceInfo.hash).thenReturn(12345)

        clientInfoResponseHandler.handleResponse(mockResponseModel)

        verify(mockDeviceInfoHashStorage).set(12345)
    }
}