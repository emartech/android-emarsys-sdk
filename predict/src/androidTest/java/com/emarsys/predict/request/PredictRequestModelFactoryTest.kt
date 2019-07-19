package com.emarsys.predict.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.URLEncoder

class PredictRequestModelFactoryTest {
    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val MERCHANT_ID = "merchantId"
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
        val BASE_HEADER = mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM")

    }

    private lateinit var requestModelFactory: PredictRequestModelFactory
    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockHeaderFactory: PredictHeaderFactory
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockHeaderFactory = mock(PredictHeaderFactory::class.java).apply {
            whenever(createBaseHeader()).thenReturn(BASE_HEADER)
        }
        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        mockRequestContext = mock(PredictRequestContext::class.java).apply {
            whenever(merchantId).thenReturn(MERCHANT_ID)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
        }

        requestModelFactory = PredictRequestModelFactory(mockRequestContext, mockHeaderFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        PredictRequestModelFactory(null, mockHeaderFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_headerFactory_mustNotBeNull() {
        PredictRequestModelFactory(mockRequestContext, null)
    }

    @Test
    fun testCreateRecommendationRequest() {
        val recommendationCriteria = URLEncoder.encode("f:SEARCH,l:5,o:0", "utf-8")

        val expected = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId?f=$recommendationCriteria&q=polo%20shirt",
                RequestMethod.GET,
                null,
                BASE_HEADER,
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestModelFactory.createRecommendationRequest()

        result shouldBe expected
    }
}