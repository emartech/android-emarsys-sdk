package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class OpenIdTokenRequestMapperTest {
    private companion object {
        const val OPEN_ID_TOKEN = "openIdToken"
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hwid"
        const val APPLICATION_CODE = "applicationCode"
    }

    private lateinit var openIdTokenRequestMapper: OpenIdTokenRequestMapper
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockRequestModelHelper: RequestModelHelper


    @BeforeEach
    fun setUp() {
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
            on { openIdToken } doReturn OPEN_ID_TOKEN
        }
        mockRequestModelHelper = mock {
            on { isMobileEngageRequest(any()) } doReturn true
            on { isMobileEngageSetContactRequest(any()) } doReturn true
        }

        openIdTokenRequestMapper = OpenIdTokenRequestMapper(mockRequestContext, mockRequestModelHelper)
    }

    @Test
    fun testMap_shouldAddOpenIdHeader_whenClientRequestIsForMobileEngage() {
        val originalRequestModels = createClientRequest()

        val expectedRequestModels = createClientRequest(extraPayloads = mapOf(
                "openIdToken" to OPEN_ID_TOKEN
        ))

        val result = openIdTokenRequestMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldAddHeaders_whenCompositeRequestIsForMobileEngage() {
        val originalRequestModels = createCompositeClientRequest()

        val expectedRequestModels = createCompositeClientRequest(extraPayloads = mapOf(
                "openIdToken" to OPEN_ID_TOKEN
        ))

        val result = openIdTokenRequestMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutOpenIdHeader_whenValueIsMissing() {
        val originalRequestModels = createClientRequest()

        whenever(mockRequestContext.openIdToken).thenReturn(null)

        val result = openIdTokenRequestMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    @Test
    fun testMap_shouldIgnoreRequest_whenRequestWasNotForMobileEngage() {
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)
        val originalRequestModels = createNonMobileEngageRequest()

        val result = openIdTokenRequestMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    private fun createNonMobileEngageRequest() = RequestModel(
            "https://not-mobile-engage.com",
            RequestMethod.POST,
            null,
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )


    private fun createCompositeClientRequest(extraHeaders: Map<String, String> = mapOf(), extraPayloads: Map<String, Any> = mapOf()) = CompositeRequestModel(
            "0",
            "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client/contact",
            RequestMethod.POST,
            mapOf("contactFieldId" to "contactFieldId") + extraPayloads,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            arrayOf(REQUEST_ID)
    )

    private fun createClientRequest(extraHeaders: Map<String, String> = mapOf(), extraPayloads: Map<String, Any> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client/contact",
            RequestMethod.POST,
            mapOf("contactFieldId" to "contactFieldId") + extraPayloads,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

}