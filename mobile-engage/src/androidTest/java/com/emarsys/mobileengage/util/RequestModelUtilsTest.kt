package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class RequestModelUtilsTest {

    companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
    }

    lateinit var requestContextMock: RequestContext
    lateinit var timestampProviderMock: TimestampProvider
    lateinit var uuidProviderMock: UUIDProvider
    lateinit var deviceInfoMock: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        uuidProviderMock = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        timestampProviderMock = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        deviceInfoMock = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        requestContextMock = mock(RequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(timestampProviderMock)
            whenever(uuidProvider).thenReturn(uuidProviderMock)
            whenever(deviceInfo).thenReturn(deviceInfoMock)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenRequest_requestContext_mustNotBeNull() {
        RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenRequest_pushToken_mustNotBeNull() {
        RequestModelUtils.createSetPushTokenRequest(null, mock(RequestContext::class.java))
    }

    @Test
    fun testCreateSetPushTokenRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createSetPushTokenUrl(requestContextMock),
                RequestMethod.PUT,
                RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN),
                RequestHeaderUtils.createBaseHeaders_V3(requestContextMock),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, requestContextMock)

        result shouldBe expected
    }
}