package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.HeaderUtils
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.util.*

class RequestHeaderUtilsTest {
    private companion object {
        const val HARDWARE_ID = "hardware_id"
        const val SDK_VERSION = "sdkVersion"
        const val APPLICATION_CODE = "applicationCode"
        const val CONTACT_FIELD_ID = 3
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
    }

    private lateinit var mockRequestContext: RequestContext
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
        }

        val mockContactFieldValueStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(CONTACT_FIELD_VALUE)
        }
        mockRequestContext = mock(RequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateBaseHeaders_requestContext_mustNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V3(null)
    }

    @Test
    fun testCreateBaseHeaders_V3() {
        val headers = RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext)

        headers shouldBe mapOf(
                "X-Client-Id" to HARDWARE_ID)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateDefaultHeaders_configShouldNotBeNull() {
        RequestHeaderUtils.createDefaultHeaders(null)
    }

    @Test
    fun testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        whenever(mockDeviceInfo.isDebugMode).thenReturn(true)

        RequestHeaderUtils.createDefaultHeaders(mockRequestContext) shouldNotBe null
    }

    @Test
    fun testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        whenever(mockDeviceInfo.isDebugMode).thenReturn(true)
        val expected = mapOf(
                "Content-Type" to "application/json",
                "X-EMARSYS-SDK-VERSION" to SDK_VERSION,
                "X-EMARSYS-SDK-MODE" to "debug")

        val result = RequestHeaderUtils.createDefaultHeaders(mockRequestContext)

        result shouldBe expected
    }

    @Test
    fun testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        whenever(mockDeviceInfo.isDebugMode).thenReturn(false)

        val expected = mapOf(
                "Content-Type" to "application/json",
                "X-EMARSYS-SDK-VERSION" to SDK_VERSION,
                "X-EMARSYS-SDK-MODE" to "production")

        val result = RequestHeaderUtils.createDefaultHeaders(mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateBaseHeaders_V2_requestContext_mustNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V2(null)
    }

    @Test
    fun testCreateBaseHeaders_V2_shouldReturnCorrectMap() {
        val expected = HashMap<String, String>()
        expected["Authorization"] = HeaderUtils.createBasicAuth(mockRequestContext.applicationCode)

        val result = RequestHeaderUtils.createBaseHeaders_V2(mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateInboxHeaders_requestContext_mustNotBeNull() {
        RequestHeaderUtils.createInboxHeaders(null)
    }

    @Test
    fun testCreateInboxHeaders() {
        val expected = mapOf(
                "x-ems-me-hardware-id" to HARDWARE_ID,
                "x-ems-me-application-code" to APPLICATION_CODE,
                "x-ems-me-contact-field-id" to CONTACT_FIELD_ID.toString(),
                "x-ems-me-contact-field-value" to CONTACT_FIELD_VALUE,
                "Content-Type" to "application/json",
                "X-EMARSYS-SDK-VERSION" to SDK_VERSION,
                "X-EMARSYS-SDK-MODE" to "production",
                "Authorization" to HeaderUtils.createBasicAuth(mockRequestContext.applicationCode)
        )

        val result = RequestHeaderUtils.createInboxHeaders(mockRequestContext)

        result shouldBe expected
    }

}