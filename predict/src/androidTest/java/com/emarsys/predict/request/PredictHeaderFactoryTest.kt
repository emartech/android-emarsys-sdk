package com.emarsys.predict.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class PredictHeaderFactoryTest {
    private companion object {
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
    }

    private lateinit var headerFactory: PredictHeaderFactory
    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockDeviceInfo = Mockito.mock(DeviceInfo::class.java).apply {
            whenever(platform).thenReturn(PLATFORM)
            whenever(osVersion).thenReturn(OS_VERSION)
        }

        mockRequestContext = Mockito.mock(PredictRequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
        }
        headerFactory = PredictHeaderFactory(mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        PredictHeaderFactory(null)
    }

    @Test
    fun testCreateBaseHeader() {
        val expected = mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM")

        val result = headerFactory.createBaseHeader()

        result shouldBe expected
    }
}