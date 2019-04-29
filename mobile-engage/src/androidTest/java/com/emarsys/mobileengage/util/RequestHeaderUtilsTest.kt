package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.mobileengage.BuildConfig
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class RequestHeaderUtilsTest {
    private companion object {
        const val HARDWARE_ID = "hardware_id"
    }

    private lateinit var requestContextMock: RequestContext
    private lateinit var deviceInfoMock: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {

        deviceInfoMock = Mockito.mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        requestContextMock = Mockito.mock(RequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(deviceInfoMock)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateBaseHeaders_requestContext_mustNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V3(null)
    }

    @Test
    fun testCreateBaseHeaders_V3() {
        val headers = RequestHeaderUtils.createBaseHeaders_V3(requestContextMock)

        headers shouldBe mapOf(
                "X-Client-Id" to HARDWARE_ID)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateDefaultHeaders_configShouldNotBeNull() {
        RequestHeaderUtils.createDefaultHeaders(null)
    }

    @Test
    fun testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        whenever(deviceInfoMock.isDebugMode).thenReturn(true)

        RequestHeaderUtils.createDefaultHeaders(requestContextMock) shouldNotBe null
    }

    @Test
    fun testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        whenever(deviceInfoMock.isDebugMode).thenReturn(true)
        val expected = mapOf(
                "Content-Type" to "application/json",
                "X-EMARSYS-SDK-VERSION" to BuildConfig.VERSION_NAME,
                "X-EMARSYS-SDK-MODE" to "debug")

        val result = RequestHeaderUtils.createDefaultHeaders(requestContextMock)

        result shouldBe expected
    }

    @Test
    fun testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        whenever(deviceInfoMock.isDebugMode).thenReturn(false)

        val expected = mapOf(
                "Content-Type" to "application/json",
                "X-EMARSYS-SDK-VERSION" to BuildConfig.VERSION_NAME,
                "X-EMARSYS-SDK-MODE" to "production")

        val result = RequestHeaderUtils.createDefaultHeaders(requestContextMock)

        result shouldBe expected
    }
}