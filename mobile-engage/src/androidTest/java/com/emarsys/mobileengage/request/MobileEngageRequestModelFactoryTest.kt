package com.emarsys.mobileengage.request

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class MobileEngageRequestModelFactoryTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
        const val REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        const val MOBILE_ENGAGE_V2_HOST = "https://push.eservice.emarsys.net/api/mobileengage/v2/"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/api/"
        const val INBOX_V3_HOST = "https://me-inbox.eservice.emarsys.net/v3"
        val CLICKS = listOf(
                ButtonClicked("campaignId1", "buttonId1", 1L),
                ButtonClicked("campaignId2", "buttonId2", 2L),
                ButtonClicked("campaignId3", "buttonId3", 3L))
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
    lateinit var mockMobileEngageV2Provider: ServiceEndpointProvider
    lateinit var mockInboxServiceProvider: ServiceEndpointProvider
    lateinit var mockButtonClickedRepository: ButtonClickedRepository

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockEventServiceProvider = mock {
            on { provideEndpointHost() } doReturn EVENT_HOST
        }
        mockClientServiceProvider = mock {
            on { provideEndpointHost() } doReturn CLIENT_HOST
        }
        mockMobileEngageV2Provider = mock {
            on { provideEndpointHost() } doReturn MOBILE_ENGAGE_V2_HOST
        }
        mockInboxServiceProvider = mock {
            on { provideEndpointHost() } doReturn INBOX_HOST
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
            on { hwid } doReturn HARDWARE_ID
            on { isDebugMode } doReturn true
            on { notificationSettings } doReturn mockNotificationSettings
        }
        mockRefreshTokenStorage = mock {
            on { get() } doReturn REFRESH_TOKEN
        }
        mockMessageInboxServiceProvider = mock {
            on { provideEndpointHost() } doReturn INBOX_V3_HOST
        }

        val mockContactFieldValueStorage: StringStorage = mock {
            on { get() } doReturn CONTACT_FIELD_VALUE
        }

        mockRequestContext = mock {
            on { timestampProvider } doReturn mockTimestampProvider
            on { uuidProvider } doReturn mockUuidProvider
            on { deviceInfo } doReturn mockDeviceInfo
            on { applicationCode } doReturn APPLICATION_CODE
            on { refreshTokenStorage } doReturn mockRefreshTokenStorage
            on { contactFieldValueStorage } doReturn mockContactFieldValueStorage
            on { sessionIdHolder } doReturn mock()
        }

        mockButtonClickedRepository = mock {
            on { query(any()) } doReturn CLICKS
        }

        requestFactory = MobileEngageRequestModelFactory(mockRequestContext, mockClientServiceProvider, mockEventServiceProvider, mockMobileEngageV2Provider, mockInboxServiceProvider, mockMessageInboxServiceProvider, mockButtonClickedRepository)
    }

    @Test
    fun testCreateSetPushTokenRequest() {
        val expected = RequestModel(
                "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/push-token",
                RequestMethod.PUT,
                RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
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
                "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/push-token", RequestMethod.DELETE,
                null,
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
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
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
        val result = requestFactory.createTrackDeviceInfoRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateSetContactRequest() {
        val expected = RequestModel(
                "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact",
                RequestMethod.POST,
                RequestPayloadUtils.createSetContactPayload("contactFieldValue", mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
        val result = requestFactory.createSetContactRequest("contactFieldValue")

        result shouldBe expected
    }

    @Test
    fun testCreateSetContactRequest_withoutContactFieldValue() {
        val expected = RequestModel(
                "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact?anonymous=true",
                RequestMethod.POST,
                emptyMap(),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
        val result = requestFactory.createSetContactRequest(null)

        result shouldBe expected
    }

    @Test
    fun testCreateCustomEventRequest() {
        val expected = RequestModel(
                "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
                RequestMethod.POST,
                RequestPayloadUtils.createCustomEventPayload("eventName", emptyMap(), mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestFactory.createCustomEventRequest("eventName", emptyMap())

        result shouldBe expected
    }

    @Test
    fun testCreateInternalCustomEventRequest() {
        val expected = RequestModel(
                "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
                RequestMethod.POST,
                RequestPayloadUtils.createInternalCustomEventPayload("eventName", emptyMap(), mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
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
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + RequestHeaderUtils.createDefaultHeaders(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestFactory.createRefreshContactTokenRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateTrackNotificationOpenRequest() {
        val expected = RequestModel(
                "https://push.eservice.emarsys.net/api/mobileengage/v2/events/message_open",
                RequestMethod.POST,
                RequestPayloadUtils.createTrackNotificationOpenPayload("sid", mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V2(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestFactory.createTrackNotificationOpenRequest("sid")

        result shouldBe expected
    }

    @Test
    fun testCreateResetBadgeCountRequest() {
        val expected = RequestModel(
                "https://me-inbox.eservice.emarsys.net/api/reset-badge-count",
                RequestMethod.POST,
                null,
                RequestHeaderUtils.createInboxHeaders(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestFactory.createResetBadgeCountRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchNotificationsRequest() {
        val expected = RequestModel(
                "https://me-inbox.eservice.emarsys.net/api/notifications",
                RequestMethod.GET,
                null,
                RequestHeaderUtils.createInboxHeaders(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestFactory.createFetchNotificationsRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchInboxMessagesRequest() {
        val expected = RequestModel(
                "https://me-inbox.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/inbox",
                RequestMethod.GET,
                null,
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = requestFactory.createFetchInboxMessagesRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchGeofenceRequest() {
        val expected = RequestModel.Builder(mockRequestContext.timestampProvider, mockRequestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url("https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/geo-fences")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext))
                .build()

        val result = requestFactory.createFetchGeofenceRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateFetchInlineInAppMessagesRequest() {
        val viewId = "testViewId"
        val expected = RequestModel.Builder(mockRequestContext.timestampProvider, mockRequestContext.uuidProvider)
                .method(RequestMethod.POST)
                .payload(RequestPayloadUtils.createInlineInAppPayload(viewId, CLICKS))
                .url("https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/inline-messages")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + RequestHeaderUtils.createDefaultHeaders(mockRequestContext))
                .build()

        val result = requestFactory.createFetchInlineInAppMessagesRequest(viewId)

        result shouldBe expected
    }
}