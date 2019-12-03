package com.emarsys

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class EmarsysRequestModelFactoryTest {

    companion object {
        const val HARDWARE_ID = "hardware_id"
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
    }

    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockTimeStampProvider: TimestampProvider
    private lateinit var requestFactory: EmarsysRequestModelFactory
    private lateinit var mockMobileEngageRequestContext: MobileEngageRequestContext

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockTimeStampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockUUIDProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        mockMobileEngageRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(mockTimeStampProvider)
            whenever(uuidProvider).thenReturn(mockUUIDProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
        }

        requestFactory = EmarsysRequestModelFactory(mockMobileEngageRequestContext)
    }

    @Test
    fun testCreateRemoteConfigRequest() {
        val expected = RequestModel.Builder(mockMobileEngageRequestContext.timestampProvider, mockMobileEngageRequestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url("https://api.myjson.com/bins/s6was")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(mockMobileEngageRequestContext))
                .build()

        val result = requestFactory.createRemoteConfigRequest()

        result shouldBe expected
    }
}