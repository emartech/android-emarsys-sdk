package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class
MobileEngageHeaderMapperTest {
    private companion object {
        const val CLIENT_STATE = "client-state"
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hwid"
        const val APPLICATION_CODE = "applicationCode"
    }

    private lateinit var mobileEngageHeaderMapper: MobileEngageHeaderMapper
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockClientStateStorage: StringStorage
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockRequestModelHelper: RequestModelHelper


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockClientStateStorage = mock {
            on { get() } doReturn CLIENT_STATE
        }

        mockDeviceInfo = mock {
            on { hardwareId } doReturn HARDWARE_ID
        }

        mockUuidProvider = mock {
            on { provideId() } doReturn REQUEST_ID
        }
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }

        mockRequestContext = mock {
            on { applicationCode } doReturn APPLICATION_CODE
            on { timestampProvider } doReturn mockTimestampProvider
            on { uuidProvider } doReturn mockUuidProvider
            on { deviceInfo } doReturn mockDeviceInfo
            on { clientStateStorage } doReturn mockClientStateStorage
        }
        mockRequestModelHelper = mock {
            on { isMobileEngageRequest(any()) } doReturn true
        }

        mobileEngageHeaderMapper = MobileEngageHeaderMapper(mockRequestContext, mockRequestModelHelper)
    }

    @Test
    fun testMap_shouldAddHeaders_whenRequestIsForMobileEngage() {
        val originalRequestModels = createMobileEngageRequest()

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Client-Id" to HARDWARE_ID,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
        result shouldNotBe originalRequestModels
    }

    @Test
    fun testMap_shouldAddHeaders_whenCompositeRequestIsForMobileEngage() {
        val originalRequestModels = createCustomEventCompositeRequest()

        val expectedRequestModels = createCustomEventCompositeRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Client-Id" to HARDWARE_ID,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutClientStateHeader_whenValueIsMissing() {
        val originalRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
                "X-Request-Order" to TIMESTAMP.toString(),
                "X-Client-Id" to HARDWARE_ID,
        ))

        whenever(mockClientStateStorage.get()).thenReturn(null)

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    @Test
    fun testMap_shouldIgnoreRequest_whenRequestWasNotForMobileEngage() {
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)
        val originalRequestModels = createNonMobileEngageRequest()

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    private fun createMobileEngageRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client",
            RequestMethod.POST,
            null,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

    private fun createCustomEventCompositeRequest(extraHeaders: Map<String, String> = mapOf()) = CompositeRequestModel(
            "0",
            "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
            RequestMethod.POST,
            null,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            arrayOf(REQUEST_ID)
    )

    private fun createNonMobileEngageRequest() = RequestModel(
            "https://not-mobile-engage.com",
            RequestMethod.POST,
            null,
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

}