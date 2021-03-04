package com.emarsys.mobileengage.request.mapper

import android.os.Looper
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.testUtil.DependencyTestUtils
import com.emarsys.mobileengage.util.RequestHeaderUtils
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


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
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

        DependencyTestUtils.setupDependencyInjectionWithServiceProviders()

        openIdTokenRequestMapper = OpenIdTokenRequestMapper(mockRequestContext)
    }

    @After
    fun tearDown() {
        val handler = getDependency<CoreSdkHandler>()
        val looper: Looper = handler.looper
        looper.quit()
        DependencyInjection.tearDown()
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
        val originalRequestModels = createNonMobileEngageRequest()

        val result = openIdTokenRequestMapper.map(originalRequestModels)

        result shouldBe originalRequestModels
    }

    private fun createNonMobileEngageRequest() = RequestModel(
            "https://not-mobile-engage.com",
            RequestMethod.POST,
            null,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )


    private fun createCompositeClientRequest(extraHeaders: Map<String, String> = mapOf(), extraPayloads: Map<String, Any> = mapOf()) = CompositeRequestModel(
            "0",
            "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client/contact",
            RequestMethod.POST,
            mapOf("contactFieldId" to "contactFieldId") + extraPayloads,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            arrayOf(REQUEST_ID)
    )

    private fun createClientRequest(extraHeaders: Map<String, String> = mapOf(), extraPayloads: Map<String, Any> = mapOf()) = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client/contact",
            RequestMethod.POST,
            mapOf("contactFieldId" to "contactFieldId") + extraPayloads,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )

}