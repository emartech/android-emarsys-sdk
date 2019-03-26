package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.BuildConfig
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.util.*

class RequestHeaderUtilsTest {
    companion object {
        const val TIMESTAMP = 123456789L
        const val HARDWARE_ID = "hardware_id"
    }

    lateinit var requestContextMock: RequestContext
    lateinit var deviceInfoMock: DeviceInfo
    lateinit var timestampProviderMock: TimestampProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {

        deviceInfoMock = Mockito.mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }
        timestampProviderMock = Mockito.mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        requestContextMock = Mockito.mock(RequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(deviceInfoMock)
            whenever(timestampProvider).thenReturn(timestampProviderMock)
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
                "X-Client-Id" to HARDWARE_ID,
                "X-Request-Order" to TIMESTAMP.toString())
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
        val expected = HashMap<String, String>()
        expected["Content-Type"] = "application/json"
        expected["X-EMARSYS-SDK-VERSION"] = BuildConfig.VERSION_NAME
        expected["X-EMARSYS-SDK-MODE"] = "debug"

        val result = RequestHeaderUtils.createDefaultHeaders(requestContextMock)

        result shouldBe expected
    }

    @Test
    fun testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        whenever(deviceInfoMock.isDebugMode).thenReturn(false)

        val expected = HashMap<String, String>()
        expected["Content-Type"] = "application/json"
        expected["X-EMARSYS-SDK-VERSION"] = BuildConfig.VERSION_NAME
        expected["X-EMARSYS-SDK-MODE"] = "production"

        val result = RequestHeaderUtils.createDefaultHeaders(requestContextMock)

        result shouldBe expected
    }
}