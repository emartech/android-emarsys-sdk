package com.emarsys.mobileengage.util


import androidx.test.filters.SdkSuppress
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.iam.model.IamConversionUtils
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.mobileengage.testUtil.RandomMETestUtils
import com.emarsys.testUtil.RandomTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

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
        const val SESSION_ID = "testSessionId"
    }

    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockRefreshTokenStorage: StringStorage
    private lateinit var mockContactFieldValueStorage: StringStorage
    private lateinit var mockNotificationSettings: NotificationSettings
    private lateinit var mockChannelSettings: List<ChannelSettings>
    private lateinit var mockSessionIdHolder: SessionIdHolder

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
        mockNotificationSettings = mock {
            on { areNotificationsEnabled() } doReturn ARE_NOTIFICATIONS_ENABLED
            on { importance } doReturn IMPORTANCE
            on { channelSettings } doReturn mockChannelSettings
        }

        mockDeviceInfo = mock {
            on { platform } doReturn PLATFORM
            on { applicationVersion } doReturn APPLICATION_VERSION
            on { model } doReturn DEVICE_MODEL
            on { osVersion } doReturn OS_VERSION
            on { sdkVersion } doReturn SDK_VERSION
            on { language } doReturn LANGUAGE
            on { timezone } doReturn TIMEZONE
            on { hardwareId } doReturn HARDWARE_ID
            on { notificationSettings } doReturn mockNotificationSettings
        }

        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }

        mockRefreshTokenStorage = mock {
            on { get() } doReturn REFRESH_TOKEN
        }
        mockContactFieldValueStorage = mock()

        mockSessionIdHolder = mock {
            on { sessionId } doReturn SESSION_ID
        }

        mockRequestContext = mock {
            on { applicationCode } doReturn APPLICATION_CODE
            on { deviceInfo } doReturn mockDeviceInfo
            on { contactFieldId } doReturn (CONTACT_FIELD_ID)
            on { timestampProvider } doReturn mockTimestampProvider
            on { refreshTokenStorage } doReturn mockRefreshTokenStorage
            on { contactFieldValueStorage } doReturn mockContactFieldValueStorage
            on { sessionIdHolder } doReturn mockSessionIdHolder
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
    fun testCreateCustomEventPayload_whenEventAttributesIsNull() {
        val event = mapOf(
                "type" to "custom",
                "name" to EVENT_NAME,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP),
                "sessionId" to SESSION_ID
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
                "attributes" to attribute,
                "sessionId" to SESSION_ID
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
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP),
                "sessionId" to SESSION_ID
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
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP),
                "sessionId" to SESSION_ID
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
    fun testCreateCustomEventPayload_shouldNotIncludeSessionId_whenItIsNull() {
        whenever(mockSessionIdHolder.sessionId).thenReturn(null)

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
                "attributes" to attribute,
                "sessionId" to SESSION_ID
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
                "timestamp" to TimestampUtils.formatTimestampWithUTC(TIMESTAMP),
                "sessionId" to SESSION_ID
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
                true
        )

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
                false
        )

        resultPayload shouldBe expectedPayload
    }

    @Test
    fun testCreateCompositeRequestModelPayload_containsDeviceEventState_whenItsNotNull() {
        val deviceEventState = "{'123': '456', '78910':'6543'}"
        val events = listOf(
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap()
        )

        val resultPayload = RequestPayloadUtils.createCompositeRequestModelPayload(
                events,
                listOf(),
                listOf(),
                false,
                deviceEventState
        )

        val expected = mapOf(
                "123" to "456",
                "78910" to "6543"
        )

        resultPayload["deviceEventState"] shouldBe expected
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

    @Test
    fun testCreateInlineInAppPayload() {
        val viewId = "testViewId"
        val clicks = listOf(
                ButtonClicked("campaignId1", "buttonId1", 1L),
                ButtonClicked("campaignId2", "buttonId2", 2L),
                ButtonClicked("campaignId3", "buttonId3", 3L)
        )
        val expectedPayload = mapOf(
                "viewIds" to listOf(viewId),
                "clicks" to clicks
        )

        val resultPayload = RequestPayloadUtils.createInlineInAppPayload(viewId, clicks)

        resultPayload shouldBe expectedPayload
    }

    @Test
    fun testEnum_eventType() {
        EventType.CUSTOM.eventType() shouldBe "custom"
        EventType.INTERNAL.eventType() shouldBe "internal"
    }
}