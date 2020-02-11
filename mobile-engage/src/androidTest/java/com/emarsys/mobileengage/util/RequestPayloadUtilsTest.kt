package com.emarsys.mobileengage.util


import androidx.test.filters.SdkSuppress
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.iam.model.IamConversionUtils
import com.emarsys.mobileengage.testUtil.RandomMETestUtils
import com.emarsys.testUtil.RandomTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class RequestPayloadUtilsTest {
    private companion object {
        const val PUSH_TOKEN = "pushToken"
        const val APPLICATION_CODE = "applicationCode"
        const val HARDWARE_ID = "hardwareId"
        const val PLATFORM = "android"
        const val APPLICATION_VERSION = "1.0.2"
        const val DEVICE_MODEL = "GT-9100"
        const val OS_VERSION = "9.0"
        const val SDK_VERSION = "1.7.2"
        const val LANGUAGE = "en-US"
        const val TIMEZONE = "+0200"
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
        const val CONTACT_FIELD_ID = 3
        const val EVENT_NAME = "testEventName"
        const val TIMESTAMP = 123456789L
        const val REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        const val SID = "sid"
        const val ARE_NOTIFICATIONS_ENABLED = true
        const val IMPORTANCE = 0
        const val CHANNEL_ID_1 = "channelId1"
        const val CHANNEL_ID_2 = "channelId2"
    }

    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockRefreshTokenStorage: Storage<String>
    private lateinit var mockContactFieldValueStorage: Storage<String>
    private lateinit var mockNotificationSettings: NotificationSettings
    private lateinit var mockChannelSettings: List<ChannelSettings>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockChannelSettings = listOf(
                ChannelSettings(channelId = CHANNEL_ID_1,
                        importance = IMPORTANCE,
                        isCanBypassDnd = true,
                        isCanShowBadge = true,
                        isShouldShowLights = true,
                        isShouldVibrate = true),
                ChannelSettings(channelId = CHANNEL_ID_2,
                        importance = IMPORTANCE))
        mockNotificationSettings = mock(NotificationSettings::class.java).apply {
            whenever(areNotificationsEnabled()).thenReturn(ARE_NOTIFICATIONS_ENABLED)
            whenever(importance).thenReturn(IMPORTANCE)
            whenever(channelSettings).thenReturn(mockChannelSettings)
        }

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(platform).thenReturn(PLATFORM)
            whenever(applicationVersion).thenReturn(APPLICATION_VERSION)
            whenever(model).thenReturn(DEVICE_MODEL)
            whenever(osVersion).thenReturn(OS_VERSION)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
            whenever(language).thenReturn(LANGUAGE)
            whenever(timezone).thenReturn(TIMEZONE)
            whenever(hwid).thenReturn(HARDWARE_ID)
            whenever(notificationSettings).thenReturn(mockNotificationSettings)
        }

        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockRefreshTokenStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(REFRESH_TOKEN)
        }
        mockContactFieldValueStorage = mock(Storage::class.java) as Storage<String>

        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
        }
    }

    @Test
    fun testCreateBasePayload_shouldReturnTheCorrectPayload_withoutLogin() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val payload = RequestPayloadUtils.createBasePayload(mockRequestContext)
        val expected = mapOf(
                "application_id" to APPLICATION_CODE,
                "hardware_id" to HARDWARE_ID
        )
        payload shouldBe expected
    }

    @Test
    fun testCreateBasePayload_shouldReturnTheCorrectPayload_withLogin() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(CONTACT_FIELD_VALUE)

        val payload = RequestPayloadUtils.createBasePayload(mockRequestContext)
        val expected = mapOf(
                "application_id" to APPLICATION_CODE,
                "hardware_id" to HARDWARE_ID,
                "contact_field_id" to CONTACT_FIELD_ID,
                "contact_field_value" to CONTACT_FIELD_VALUE
        )
        payload shouldBe expected
    }

    @Test
    fun testCreateSetPushTokenPayload() {
        val payload = RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN)
        payload shouldBe mapOf(
                "pushToken" to PUSH_TOKEN
        )
    }

    @Test
    @SdkSuppress(minSdkVersion = android.os.Build.VERSION_CODES.O)
    fun testCreateTrackDeviceInfoPayload() {
        val payload = RequestPayloadUtils.createTrackDeviceInfoPayload(mockRequestContext)
        payload shouldBe mapOf(
                "platform" to PLATFORM,
                "applicationVersion" to APPLICATION_VERSION,
                "deviceModel" to DEVICE_MODEL,
                "osVersion" to OS_VERSION,
                "sdkVersion" to SDK_VERSION,
                "language" to LANGUAGE,
                "timezone" to TIMEZONE,
                "pushSettings" to mapOf(
                        "areNotificationsEnabled" to ARE_NOTIFICATIONS_ENABLED,
                        "importance" to IMPORTANCE,
                        "channelSettings" to listOf(
                                mapOf("channelId" to CHANNEL_ID_1,
                                        "importance" to IMPORTANCE,
                                        "canShowBadge" to true,
                                        "canBypassDnd" to true,
                                        "shouldVibrate" to true,
                                        "shouldShowLights" to true
                                ),
                                mapOf("channelId" to CHANNEL_ID_2,
                                        "importance" to IMPORTANCE,
                                        "canShowBadge" to false,
                                        "canBypassDnd" to false,
                                        "shouldVibrate" to false,
                                        "shouldShowLights" to false
                                )
                        )
                )
        )

    }

    @Test
    @SdkSuppress(maxSdkVersion = android.os.Build.VERSION_CODES.N)
    fun testCreateTrackDeviceInfoPayload_belowOreo() {
        val payload = RequestPayloadUtils.createTrackDeviceInfoPayload(mockRequestContext)
        payload shouldBe mapOf(
                "platform" to PLATFORM,
                "applicationVersion" to APPLICATION_VERSION,
                "deviceModel" to DEVICE_MODEL,
                "osVersion" to OS_VERSION,
                "sdkVersion" to SDK_VERSION,
                "language" to LANGUAGE,
                "timezone" to TIMEZONE,
                "pushSettings" to mapOf(
                        "areNotificationsEnabled" to ARE_NOTIFICATIONS_ENABLED,
                        "importance" to IMPORTANCE
                )
        )

    }

    @Test
    fun testCreateSetContactPayload() {
        val payload = RequestPayloadUtils.createSetContactPayload(CONTACT_FIELD_VALUE, mockRequestContext)
        payload shouldBe mapOf(
                "contactFieldId" to CONTACT_FIELD_ID,
                "contactFieldValue" to CONTACT_FIELD_VALUE
        )
    }

    @Test
    fun testCreateCustomEventPayload_whenEventAttributesIsNull() {
        val event = mapOf(
                "type" to "custom",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP)
        )

        val expectedPayload = mapOf<String, Any>(
                "clicks" to emptyList<Any>(),
                "viewedMessages" to emptyList<Any>(),
                "events" to listOf(event)
        )

        val actualPayload = RequestPayloadUtils.createCustomEventPayload(EVENT_NAME, null, mockRequestContext)

        actualPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateCustomEventPayload_whenEventAttributesIsPresent() {
        val attribute = mapOf("attributeKey" to "attributeValue")

        val event = mapOf(
                "type" to "custom",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP),
                "attributes" to attribute
        )

        val expectedPayload = mapOf<String, Any>(
                "clicks" to emptyList<Any>(),
                "viewedMessages" to emptyList<Any>(),
                "events" to listOf(event)
        )

        val actualPayload = RequestPayloadUtils.createCustomEventPayload(EVENT_NAME, attribute, mockRequestContext)

        actualPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateCustomEventPayload_whenEventAttributesIsEmpty() {
        val event = mapOf(
                "type" to "custom",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP)
        )

        val expectedPayload = mapOf<String, Any>(
                "clicks" to emptyList<Any>(),
                "viewedMessages" to emptyList<Any>(),
                "events" to listOf(event)
        )

        val actualPayload = RequestPayloadUtils.createCustomEventPayload(EVENT_NAME, emptyMap(), mockRequestContext)

        actualPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateInternalCustomEventPayload_whenEventAttributesIsNull() {
        val event = mapOf(
                "type" to "internal",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP)
        )

        val expectedPayload = mapOf<String, Any>(
                "clicks" to emptyList<Any>(),
                "viewedMessages" to emptyList<Any>(),
                "events" to listOf(event)
        )

        val actualPayload = RequestPayloadUtils.createInternalCustomEventPayload(EVENT_NAME, null, mockRequestContext)

        actualPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateInternalCustomEventPayload_whenEventAttributesIsPresent() {
        val attribute = mapOf("attributeKey" to "attributeValue")

        val event = mapOf(
                "type" to "internal",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP),
                "attributes" to attribute
        )

        val expectedPayload = mapOf<String, Any>(
                "clicks" to emptyList<Any>(),
                "viewedMessages" to emptyList<Any>(),
                "events" to listOf(event)
        )

        val actualPayload = RequestPayloadUtils.createInternalCustomEventPayload(EVENT_NAME, attribute, mockRequestContext)

        actualPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateInternalCustomEventPayload_whenEventAttributesIsEmpty() {
        val event = mapOf(
                "type" to "internal",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP)
        )

        val expectedPayload = mapOf<String, Any>(
                "clicks" to emptyList<Any>(),
                "viewedMessages" to emptyList<Any>(),
                "events" to listOf(event)
        )

        val actualPayload = RequestPayloadUtils.createInternalCustomEventPayload(EVENT_NAME, emptyMap(), mockRequestContext)

        actualPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateCompositeRequestModelPayload_payloadContainsDoNotDisturb_whenDoNotDisturbIsTrue() {
        val payload = RequestPayloadUtils.createCompositeRequestModelPayload(
                emptyList(),
                emptyList(),
                emptyList(),
                true)

        (payload["dnd"] as Boolean) shouldBe true
    }

    @Test
    fun testCreateCompositeRequestModelPayload() {
        val events = listOf(
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap()
        )
        val displayedIams = listOf(
                RandomMETestUtils.randomDisplayedIam(),
                RandomMETestUtils.randomDisplayedIam()
        )
        val buttonClicks = listOf(
                RandomMETestUtils.randomButtonClick(),
                RandomMETestUtils.randomButtonClick(),
                RandomMETestUtils.randomButtonClick()
        )
        val expectedPayload = mapOf(
                "events" to events,
                "viewedMessages" to IamConversionUtils.displayedIamsToArray(displayedIams),
                "clicks" to IamConversionUtils.buttonClicksToArray(buttonClicks)
        )

        val resultPayload = RequestPayloadUtils.createCompositeRequestModelPayload(
                events,
                displayedIams,
                buttonClicks,
                false)

        resultPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateRefreshContactTokenPayload() {
        val expectedPayload = mapOf(
                "refreshToken" to REFRESH_TOKEN
        )

        val resultPayload = RequestPayloadUtils.createRefreshContactTokenPayload(mockRequestContext)

        resultPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateTrackNotificationOpenPayload() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(CONTACT_FIELD_VALUE)

        val expectedPayload = mapOf(
                "application_id" to APPLICATION_CODE,
                "hardware_id" to HARDWARE_ID,
                "contact_field_id" to CONTACT_FIELD_ID,
                "contact_field_value" to CONTACT_FIELD_VALUE,
                "source" to "inbox",
                "sid" to SID
        )

        val resultPayload = RequestPayloadUtils.createTrackNotificationOpenPayload(SID, mockRequestContext)

        resultPayload shouldBe expectedPayload
    }
}