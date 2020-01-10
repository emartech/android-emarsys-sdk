package com.emarsys.mobileengage.request

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

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
    }

    lateinit var mockRequestContext: MobileEngageRequestContext
    lateinit var mockTimestampProvider: TimestampProvider
    lateinit var mockUuidProvider: UUIDProvider
    lateinit var mockDeviceInfo: DeviceInfo
    lateinit var mockRefreshTokenStorage: Storage<String>
    lateinit var mockNotificationSettings: NotificationSettings
    lateinit var requestFactory: MobileEngageRequestModelFactory
    lateinit var eventServiceProvider: ServiceEndpointProvider
    lateinit var clientServiceProvider: ServiceEndpointProvider
    lateinit var mobileEngageV2Provider: ServiceEndpointProvider
    lateinit var inboxServiceProvider: ServiceEndpointProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        eventServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(EVENT_HOST)
        }
        clientServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(CLIENT_HOST)
        }
        mobileEngageV2Provider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(MOBILE_ENGAGE_V2_HOST)
        }
        inboxServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(INBOX_HOST)
        }

        mockNotificationSettings = mock(NotificationSettings::class.java).apply {
            whenever(channelSettings).thenReturn(listOf())
            whenever(importance).thenReturn(0)
        }
        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
            whenever(isDebugMode).thenReturn(true)
            whenever(notificationSettings).thenReturn(mockNotificationSettings)
        }
        mockRefreshTokenStorage = mock(StringStorage::class.java).apply {
            whenever(get()).thenReturn(REFRESH_TOKEN)
        }

        val mockContactFieldValueStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(CONTACT_FIELD_VALUE)
        }

        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
        }

        requestFactory = MobileEngageRequestModelFactory(mockRequestContext, clientServiceProvider, eventServiceProvider, mobileEngageV2Provider, inboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageRequestModelFactory(null, clientServiceProvider, eventServiceProvider, mobileEngageV2Provider, inboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_clientServiceProvider_mustNotBeNull() {
        MobileEngageRequestModelFactory(mockRequestContext, null, eventServiceProvider, mobileEngageV2Provider, inboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_eventServiceProvider_mustNotBeNull() {
        MobileEngageRequestModelFactory(mockRequestContext, clientServiceProvider, null, mobileEngageV2Provider, inboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageV2Provider_mustNotBeNull() {
        MobileEngageRequestModelFactory(mockRequestContext, clientServiceProvider, eventServiceProvider, null, inboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_inboxServiceProvider_mustNotBeNull() {
        MobileEngageRequestModelFactory(mockRequestContext, clientServiceProvider, eventServiceProvider, mobileEngageV2Provider, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenRequest_pushToken_mustNotBeNull() {
        requestFactory.createSetPushTokenRequest(null)
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

    @Test(expected = IllegalArgumentException::class)
    fun testCreateCustomEvent_eventName_mustNotBeNull() {
        requestFactory.createCustomEventRequest(null, emptyMap())
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

    @Test(expected = IllegalArgumentException::class)
    fun testCreateInternalCustomEventRequest_eventName_mustNotBeNull() {
        requestFactory.createInternalCustomEventRequest(null, emptyMap())
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

    @Test(expected = IllegalArgumentException::class)
    fun testCreateTrackNotificationOpenRequest_sid_mustNotBeNull() {
        requestFactory.createTrackNotificationOpenRequest(null)
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
}