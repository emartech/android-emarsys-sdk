package com.emarsys.mobileengage.request

import android.os.Handler
import android.os.Looper
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.testUtil.DependencyTestUtils
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class MobileEngageHeaderMapperTest {

    private companion object {
        const val CLIENT_STATE = "client-state"
        const val CONTACT_TOKEN = "contact-token"
        const val ID_TOKEN = "id-token"
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
    private lateinit var mockClientStateStorage: StringStorage
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockRefreshTokenStorage: StringStorage
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockClientStateStorage = mock {
            on { get() } doReturn CLIENT_STATE
        }

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
            on { clientStateStorage } doReturn mockClientStateStorage
            on { contactTokenStorage } doReturn mockContactTokenStorage
            on { refreshTokenStorage } doReturn mockRefreshTokenStorage
            on { idToken } doReturn ID_TOKEN
        }

        DependencyTestUtils.setupDependencyInjectionWithServiceProviders()

        mobileEngageHeaderMapper = MobileEngageHeaderMapper(mockRequestContext)
    }

    @After
    fun tearDown() {
        val handler = getDependency<Handler>("coreSdkHandler")
        val looper: Looper? = handler.looper
        looper?.quit()
        DependencyInjection.tearDown()
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

    @Test
    fun testMap_shouldAddOpenIdHeader_whenClientRequestIsForMobileEngage() {
        val originalRequestModels = createClientRequest()

        val expectedRequestModels = createClientRequest(extraHeaders = mapOf(
                "X-Client-State" to CLIENT_STATE,
                "X-Open-Id" to ID_TOKEN,
                "X-Request-Order" to TIMESTAMP.toString()
        ))

        val result = mobileEngageHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
    }

    private fun createClientRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact",
            RequestMethod.POST,
            null,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

    private fun createMobileEngageRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client",
            RequestMethod.POST,
            null,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )


    private fun createRefreshContactTokenRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact-token",
            RequestMethod.POST,
            RequestPayloadUtils.createRefreshContactTokenPayload(mockRequestContext),
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

    private fun createCustomEventCompositeRequest(extraHeaders: Map<String, String> = mapOf()) = CompositeRequestModel(
            "0",
            "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
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