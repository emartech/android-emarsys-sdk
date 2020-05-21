package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
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
    private lateinit var mockDeviceInfoPayloadStorage:StringStorage
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockDeviceInfo = mock(DeviceInfo::class.java)
        mockDeviceInfoPayloadStorage = mock(StringStorage::class.java) as StringStorage

        mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL)
        }
        mockResponseModel = mock(ResponseModel::class.java).apply {
            whenever(requestModel).thenReturn(mockRequestModel)
        }

        clientInfoResponseHandler = ClientInfoResponseHandler(mockDeviceInfo, mockDeviceInfoPayloadStorage)
    }

    @Test
    fun testShouldHandleResponse_true_whenRequestIsClientInfo() {
        val result = clientInfoResponseHandler.shouldHandleResponse(mockResponseModel)

        result shouldBe true
    }

    @Test
    fun testHandleResponse() {
        whenever(mockDeviceInfo.deviceInfoPayload).thenReturn(createDeviceInfoPayload())

        clientInfoResponseHandler.handleResponse(mockResponseModel)

        verify(mockDeviceInfoPayloadStorage).set(createDeviceInfoPayload())
    }

    private fun createDeviceInfoPayload(): String {
        return """{
                  "notificationSettings": {
                    "channelSettigns": [
                      [
                        {
                          "channelId": "ems_sample_news",
                          "importance": 4,
                          "isCanBypassDnd": false,
                          "isCanShowBadge": true,
                          "isShouldVibrate": false
                        },
                        {
                          "channelId": "ems_sample_messages",
                          "importance": 4,
                          "isCanBypassDnd": false,
                          "isCanShowBadge": true,
                          "isShouldVibrate": false
                        }
                      ]
                    ],
                    "importance": -1000,
                    "areNotificationsEnabled": true
                  },
                  "hwid": "1e1a57f2789e46ac",
                  "platform": "android",
                  "language": "en-US",
                  "timezone": "+0200",
                  "manufacturer": "Google",
                  "model": "Android SDK built for x86",
                  "osVersion": "10",
                  "displayMetrics": "1080x1794",
                  "sdkVersion": "2.5.0-5-gf8a8a5e"
                }"""
    }
}