package com.emarsys.mobileengage.request

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MobileEngageRequestModelFactoryTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
        const val REFRESH_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        const val CONTACT_FIELD_ID = 3
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val INBOX_V3_HOST = "https://me-inbox.eservice.emarsys.net/v3"
        val CLICKS = listOf(
            ButtonClicked("campaignId1", "buttonId1", 1L),
            ButtonClicked("campaignId2", "buttonId2", 2L),
            ButtonClicked("campaignId3", "buttonId3", 3L)
        )
    }

    lateinit var mockRequestContext: MobileEngageRequestContext
    lateinit var mockTimestampProvider: TimestampProvider
    lateinit var mockUuidProvider: UUIDProvider
    lateinit var mockDeviceInfo: DeviceInfo
    lateinit var mockRefreshTokenStorage: StringStorage
    lateinit var mockNotificationSettings: NotificationSettings
    lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider
    lateinit var requestFactory: MobileEngageRequestModelFactory
    lateinit var mockEventServiceProvider: ServiceEndpointProvider
    lateinit var mockClientServiceProvider: ServiceEndpointProvider
    lateinit var mockButtonClickedRepository: ButtonClickedRepository

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockEventServiceProvider = mock {
            on { provideEndpointHost() } doReturn EVENT_HOST
        }
        mockClientServiceProvider = mock {
            on { provideEndpointHost() } doReturn CLIENT_HOST
        }

        mockNotificationSettings = mock {
            on { channelSettings } doReturn listOf()
            on { importance } doReturn 0
        }
        mockUuidProvider = mock {
            on { provideId() } doReturn REQUEST_ID
        }
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }
        mockDeviceInfo = mock {
            on { hardwareId } doReturn HARDWARE_ID
            on { isDebugMode } doReturn true
            on { notificationSettings } doReturn mockNotificationSettings
        }
        mockRefreshTokenStorage = mock {
            on { get() } doReturn REFRESH_TOKEN
        }
        mockMessageInboxServiceProvider = mock {
            on { provideEndpointHost() } doReturn INBOX_V3_HOST
        }

        mockRequestContext = mock {
            on { timestampProvider } doReturn mockTimestampProvider
            on { uuidProvider } doReturn mockUuidProvider
            on { deviceInfo } doReturn mockDeviceInfo
            on { applicationCode } doReturn APPLICATION_CODE
            on { refreshTokenStorage } doReturn mockRefreshTokenStorage
            on { contactFieldValue } doReturn CONTACT_FIELD_VALUE
            on { contactFieldId } doReturn CONTACT_FIELD_ID
            on { sessionIdHolder } doReturn mock()
        }

        mockButtonClickedRepository = mock {
            on { query(any()) } doReturn CLICKS
        }

        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)

        requestFactory = MobileEngageRequestModelFactory(
            mockRequestContext,
            mockClientServiceProvider,
            mockEventServiceProvider,
            mockMessageInboxServiceProvider,
            mockButtonClickedRepository
        )
    }

    @Test
    fun testCreateSetPushTokenRequest() {
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/push-token",
            RequestMethod.PUT,
            RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createSetPushTokenRequest(PUSH_TOKEN)

        result shouldBe expected
    }

    @Test
    fun testCreateRemovePushTokenRequest() {
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/push-token",
            RequestMethod.DELETE,
            null,
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createRemovePushTokenRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateTrackDeviceInfoRequest() {
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client",
            RequestMethod.POST,
            RequestPayloadUtils.createTrackDeviceInfoPayload(mockRequestContext),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )
        val result = requestFactory.createTrackDeviceInfoRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateSetContactRequest() {
        whenever(mockRequestContext.hasContactIdentification()).doReturn(true)
        whenever(mockRequestContext.contactFieldId).doReturn(CONTACT_FIELD_ID)
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact",
            RequestMethod.POST,
            mapOf("contactFieldId" to CONTACT_FIELD_ID, "contactFieldValue" to CONTACT_FIELD_VALUE),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )
        val result = requestFactory.createSetContactRequest(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        result shouldBe expected
    }

    @Test
    fun testCreateSetContactRequest_withoutContactFieldValueAndOpenIdToken() {
        whenever(mockRequestContext.hasContactIdentification()).doReturn(false)
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact?anonymous=true",
            RequestMethod.POST,
            emptyMap(),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )
        val result = requestFactory.createSetContactRequest(null, null)

        result shouldBe expected
    }

    @Test
    fun testCreateSetContactRequest_doesFillPayload_withContactFieldValue() {
        whenever(mockRequestContext.hasContactIdentification()).doReturn(true)
        whenever(mockRequestContext.contactFieldId).doReturn(CONTACT_FIELD_ID)
        whenever(mockRequestContext.contactFieldValue).doReturn(CONTACT_FIELD_VALUE)

        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact",
            RequestMethod.POST,
            mapOf(
                "contactFieldId" to CONTACT_FIELD_ID,
                "contactFieldValue" to CONTACT_FIELD_VALUE
            ),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )
        val result = requestFactory.createSetContactRequest(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        result shouldBe expected
    }

    @Test
    fun testCreateSetContactRequest_doesNotFillPayload_withContactFieldValueMissing() {
        whenever(mockRequestContext.hasContactIdentification()).doReturn(true)
        whenever(mockRequestContext.contactFieldValue).doReturn(null)
        whenever(mockRequestContext.contactFieldId).doReturn(CONTACT_FIELD_ID)

        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact",
            RequestMethod.POST,
            mapOf(),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )
        val result = requestFactory.createSetContactRequest(null, null)

        result shouldBe expected
    }

    @Test
    fun testCreateInternalCustomEventRequest_whenEventServiceV4_isNotEnabled() {
        FeatureRegistry.disableFeature(InnerFeature.EVENT_SERVICE_V4)
        val expected = RequestModel(
            "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
            RequestMethod.POST,
            RequestPayloadUtils.createInternalCustomEventPayload(
                "eventName",
                emptyMap(),
                mockRequestContext
            ),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createInternalCustomEventRequest("eventName", emptyMap())

        result shouldBe expected
    }

    @Test
    fun testCreateCustomEventRequest_whenEventServiceV4_isEnabled() {
        val expected = RequestModel(
            "https://mobile-events.eservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/events",
            RequestMethod.POST,
            RequestPayloadUtils.createInternalCustomEventPayload(
                "eventName",
                emptyMap(),
                mockRequestContext
            ),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createInternalCustomEventRequest("eventName", emptyMap())

        result shouldBe expected
    }

    @Test
    fun testCreateInternalCustomEventRequest() {
        val expected = RequestModel(
            "https://mobile-events.eservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/events",
            RequestMethod.POST,
            RequestPayloadUtils.createInternalCustomEventPayload(
                "eventName",
                emptyMap(),
                mockRequestContext
            ),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createInternalCustomEventRequest("eventName", emptyMap())

        result shouldBe expected
    }

    @Test
    fun testCreateRefreshContactTokenRequest() {
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact-token",
            RequestMethod.POST,
            RequestPayloadUtils.createRefreshContactTokenPayload(mockRequestContext),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createRefreshContactTokenRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchInboxMessagesRequest() {
        val expected = RequestModel(
            "https://me-inbox.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/inbox",
            RequestMethod.GET,
            null,
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val result = requestFactory.createFetchInboxMessagesRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchGeofenceRequest() {
        val expected = RequestModel.Builder(
            mockRequestContext.timestampProvider,
            mockRequestContext.uuidProvider
        )
            .method(RequestMethod.GET)
            .url("https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/geo-fences")
            .build()

        val result = requestFactory.createFetchGeofenceRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchInlineInAppMessagesRequest() {
        val viewId = "testViewId"
        val expected = RequestModel.Builder(
            mockRequestContext.timestampProvider,
            mockRequestContext.uuidProvider
        )
            .method(RequestMethod.POST)
            .payload(RequestPayloadUtils.createInlineInAppPayload(viewId, CLICKS))
            .url("https://mobile-events.eservice.emarsys.net/v4/apps/$APPLICATION_CODE/inline-messages")
            .build()

        val result = requestFactory.createFetchInlineInAppMessagesRequest(viewId)

        result shouldBe expected
    }
}