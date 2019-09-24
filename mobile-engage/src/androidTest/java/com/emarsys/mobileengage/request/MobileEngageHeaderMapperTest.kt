package com.emarsys.mobileengage.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.mobileengage.util.RequestUrlUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class MobileEngageHeaderMapperTest {

    private companion object {
        const val CLIENT_STATE = "client-state"
        const val CONTACT_TOKEN = "contact-token"
        const val REFRESH_TOKEN = "refresh-token"
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hwid"
        const val APPLICATION_CODE = "applicationCode"
    }

    private lateinit var mobileEngageHeaderMapper: MobileEngageHeaderMapper

    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockClientStateStorage: Storage<String>
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var mockRefreshTokenStorage: Storage<String>
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {

        mockClientStateStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(CLIENT_STATE)
        }

        mockContactTokenStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(CONTACT_TOKEN)
        }

        mockRefreshTokenStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(REFRESH_TOKEN)
        }

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        val mockApplicationCodeStorage = (mock(Storage::class.java) as Storage<String?>).apply {
            whenever(get()).thenReturn(APPLICATION_CODE)
        }

        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCodeStorage).thenReturn(mockApplicationCodeStorage)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(clientStateStorage).thenReturn(mockClientStateStorage)
            whenever(contactTokenStorage).thenReturn(mockContactTokenStorage)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
        }

        mobileEngageHeaderMapper = MobileEngageHeaderMapper(mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageHeaderMapper(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_requestModel_mustNotBeNull() {
        mobileEngageHeaderMapper.map(null)
    }

    @Test
    fun testMap_shouldAddHeaders_whenRequestIsForMobileEngage() {
        val originalRequestModels = createMobileEngageRequest()

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Contact-Token" to CONTACT_TOKEN,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutContactTokenHeader_whenValueIsMissing() {
        val originalRequestModels = createMobileEngageRequest()

        whenever(mockContactTokenStorage.get()).thenReturn(null)

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutContactTokenHeader_whenRequestIsRefreshContactToken() {
        val originalRequestModels = createRefreshContactTokenRequest()

        val expectedRequestModels = createRefreshContactTokenRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldLeaveOutClientStateHeader_whenValueIsMissing() {
        val originalRequestModels = createMobileEngageRequest()

        whenever(mockClientStateStorage.get()).thenReturn(null)

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
                "X-Contact-Token" to CONTACT_TOKEN,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldIgnoreRequest_whenRequestWasNotForMobileEngage() {
        val originalRequestModels = createNonMobileEngageRequest()

        val expectedRequestModels = createNonMobileEngageRequest()

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    @Test
    fun testMap_shouldAddHeaders_whenCompositeRequestIsForMobileEngage() {
        val originalRequestModels = createCustomEventCompositeRequest()

        val expectedRequestModels = createCustomEventCompositeRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Contact-Token" to CONTACT_TOKEN,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    private fun createMobileEngageRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            Endpoint.ME_V3_CLIENT_BASE,
            RequestMethod.POST,
            null,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )


    private fun createRefreshContactTokenRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            RequestUrlUtils.createRefreshContactTokenUrl(mockRequestContext),
            RequestMethod.POST,
            RequestPayloadUtils.createRefreshContactTokenPayload(mockRequestContext),
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

    private fun createCustomEventCompositeRequest(extraHeaders: Map<String, String> = mapOf()) = CompositeRequestModel(
            "0",
            Endpoint.ME_V3_CLIENT_BASE,
            RequestMethod.POST,
            null,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            arrayOf(REQUEST_ID)
    )

    private fun createNonMobileEngageRequest() = RequestModel(
            "https://not-mobile-engage.com",
            RequestMethod.POST,
            null,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )
}