package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

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
                "X-CLIENT-ID" to HARDWARE_ID,
                "X-REQUEST-ORDER" to TIMESTAMP.toString())
    }
}