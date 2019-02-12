package com.emarsys.mobileengage.util


import com.emarsys.core.device.DeviceInfo
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class RequestPayloadUtilsTest {
    companion object {
        const val PUSH_TOKEN = "pushToken"
        const val PLATFORM = "android"
        const val APPLICATION_VERSION = "1.0.2"
        const val DEVICE_MODEL = "GT-9100"
        const val OS_VERSION = "9.0"
        const val SDK_VERSION = "1.7.2"
        const val LANGUAGE = "en-US"
        const val TIMEZONE = "+0200"
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
        const val CONTACT_FIELD_ID = 3
    }

    lateinit var deviceInfoMock: DeviceInfo
    lateinit var requestContextMock: RequestContext

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        deviceInfoMock = mock(DeviceInfo::class.java).apply {
            whenever(platform).thenReturn(PLATFORM)
            whenever(applicationVersion).thenReturn(APPLICATION_VERSION)
            whenever(model).thenReturn(DEVICE_MODEL)
            whenever(osVersion).thenReturn(OS_VERSION)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
            whenever(language).thenReturn(LANGUAGE)
            whenever(timezone).thenReturn(TIMEZONE)
        }

        requestContextMock = mock(RequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(deviceInfoMock)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenPayload_pushToken_mustNotBeNull() {
        RequestPayloadUtils.createSetPushTokenPayload(null)
    }

    @Test
    fun testCreateSetPushTokenPayload() {
        val payload = RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN)
        payload shouldBe mapOf(
                "pushToken" to PUSH_TOKEN
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateTrackDeviceInfoPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createTrackDeviceInfoPayload(null)
    }

    @Test
    fun testCreateTrackDeviceInfoPayload() {
        val payload = RequestPayloadUtils.createTrackDeviceInfoPayload(requestContextMock)
        payload shouldBe mapOf(
                "platform" to PLATFORM,
                "applicationVersion" to APPLICATION_VERSION,
                "deviceModel" to DEVICE_MODEL,
                "osVersion" to OS_VERSION,
                "sdkVersion" to SDK_VERSION,
                "language" to LANGUAGE,
                "timezone" to TIMEZONE
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactPayload_contactFieldValue_mustNotBeNull() {
        RequestPayloadUtils.createSetContactPayload(null, requestContextMock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createSetContactPayload(CONTACT_FIELD_VALUE, null)
    }

    @Test
    fun testCreateSetContactPayload() {
        val payload = RequestPayloadUtils.createSetContactPayload(CONTACT_FIELD_VALUE, requestContextMock)
        payload shouldBe mapOf(
                "contactFieldId" to CONTACT_FIELD_ID.toString(),
                "contactFieldValue" to CONTACT_FIELD_VALUE
        )
    }
}