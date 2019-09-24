package com.emarsys.mobileengage.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.notification.NotificationSettings
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.endpoint.Endpoint.INBOX_FETCH_V1
import com.emarsys.mobileengage.endpoint.Endpoint.INBOX_RESET_BADGE_COUNT_V1
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

class MobileEngageRequestModelFactoryTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
        const val REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
    }

    lateinit var mockRequestContext: MobileEngageRequestContext
    lateinit var mockTimestampProvider: TimestampProvider
    lateinit var mockUuidProvider: UUIDProvider
    lateinit var mockDeviceInfo: DeviceInfo
    lateinit var mockRefreshTokenStorage: Storage<String>
    lateinit var mockNotificationSettings: NotificationSettings
    lateinit var requestFactory: MobileEngageRequestModelFactory

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
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
        val mockApplicationCodeStorage = (mock(Storage::class.java) as Storage<String?>).apply {
            whenever(get()).thenReturn(APPLICATION_CODE)
        }
        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCodeStorage).thenReturn(mockApplicationCodeStorage)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
        }

        requestFactory = MobileEngageRequestModelFactory(mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageRequestModelFactory(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenRequest_pushToken_mustNotBeNull() {
        requestFactory.createSetPushTokenRequest(null)
    }

    @Test
    fun testCreateSetPushTokenRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createSetPushTokenUrl(mockRequestContext),
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
                RequestUrlUtils.createRemovePushTokenUrl(mockRequestContext),
                RequestMethod.DELETE,
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
                RequestUrlUtils.createTrackDeviceInfoUrl(mockRequestContext),
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
                RequestUrlUtils.createSetContactUrl(mockRequestContext),
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
                RequestUrlUtils.createSetContactUrl(mockRequestContext) + "?anonymous=true",
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
                RequestUrlUtils.createCustomEventUrl(mockRequestContext),
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
                RequestUrlUtils.createCustomEventUrl(mockRequestContext),
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
                RequestUrlUtils.createRefreshContactTokenUrl(mockRequestContext),
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
                RequestUrlUtils.createEventUrl_V2("message_open"),
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
                INBOX_RESET_BADGE_COUNT_V1,
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
                INBOX_FETCH_V1,
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