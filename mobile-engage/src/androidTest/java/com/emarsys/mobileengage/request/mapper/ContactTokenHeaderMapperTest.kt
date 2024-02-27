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
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ContactTokenHeaderMapperTest : AnnotationSpec() {
    private companion object {
        const val CONTACT_TOKEN = "contactToken"
        const val REFRESH_TOKEN = "refreshToken"
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hwid"
        const val APPLICATION_CODE = "applicationCode"
    }

    private lateinit var contactTokenHeaderMapper: ContactTokenHeaderMapper
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockRefreshTokenStorage: StringStorage
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockRequestModelHelper: RequestModelHelper


    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockContactTokenStorage = mock {
            on { get() } doReturn CONTACT_TOKEN
        }
        mockRefreshTokenStorage = mock {
            on { get() } doReturn REFRESH_TOKEN
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
            on { contactTokenStorage } doReturn mockContactTokenStorage
            on { refreshTokenStorage } doReturn mockRefreshTokenStorage
        }
        mockRequestModelHelper = mock {
            on { isMobileEngageRequest(any()) } doReturn true
            on { isMobileEngageSetContactRequest(any()) } doReturn false
            on { isRefreshContactTokenRequest(any()) } doReturn false
        }

        contactTokenHeaderMapper = ContactTokenHeaderMapper(mockRequestContext, mockRequestModelHelper)
    }

    @Test
    fun testMap_shouldAddHeaders_whenRequestIsForMobileEngage() {
        val originalRequestModels = createMobileEngageRequest()

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
                "X-Contact-Token" to CONTACT_TOKEN
        ))

        val result = contactTokenHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
        result shouldNotBe originalRequestModels
    }

    @Test
    fun testMap_shouldAddHeaders_whenCompositeRequestIsForMobileEngage() {
        val originalRequestModels = createCustomEventCompositeRequest()

        val expectedRequestModels = createCustomEventCompositeRequest(extraHeaders = mapOf(
                "X-Contact-Token" to CONTACT_TOKEN
        ))

        val result = contactTokenHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
        result shouldNotBe originalRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutContactTokenHeader_whenValueIsMissing() {
        val originalRequestModels = createMobileEngageRequest()

        whenever(mockContactTokenStorage.get()).thenReturn(null)

        val result = contactTokenHeaderMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutContactTokenHeader_whenRequestIsRefreshContactToken() {
        val originalRequestModels = createRefreshContactTokenRequest()

        whenever(mockContactTokenStorage.get()).thenReturn(null)

        val result = contactTokenHeaderMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    @Test
    fun testMap_shouldIgnoreRequest_whenRequestWasNotForMobileEngage() {
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)
        val originalRequestModels = createNonMobileEngageRequest()

        val result = contactTokenHeaderMapper.map(originalRequestModels)

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
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client",
            RequestMethod.POST,
            null,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            arrayOf(REQUEST_ID)
    )

    private fun createRefreshContactTokenRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client/contact-token",
            RequestMethod.POST,
            RequestPayloadUtils.createRefreshContactTokenPayload(mockRequestContext),
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
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