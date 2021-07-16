package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.HeaderUtils
import com.emarsys.mobileengage.MobileEngageRequestContext
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

    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hardwareId).thenReturn(HARDWARE_ID)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
        }

        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
            whenever(contactFieldValue).thenReturn(CONTACT_FIELD_VALUE)
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
}