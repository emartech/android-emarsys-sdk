package com.emarsys.mobileengage.util


import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class RequestPayloadUtilsTest {
    companion object {
        const val PUSH_TOKEN = "pushToken"
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
    }

    lateinit var mockDeviceInfo: DeviceInfo
    lateinit var mockRequestContext: RequestContext
    lateinit var mockTimestampProvider: TimestampProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(platform).thenReturn(PLATFORM)
            whenever(applicationVersion).thenReturn(APPLICATION_VERSION)
            whenever(model).thenReturn(DEVICE_MODEL)
            whenever(osVersion).thenReturn(OS_VERSION)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
            whenever(language).thenReturn(LANGUAGE)
            whenever(timezone).thenReturn(TIMEZONE)
        }

        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockRequestContext = mock(RequestContext::class.java).apply {
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenPayload_pushToken_mustNotBeNull() {
        RequestPayloadUtils.createSetPushTokenPayload(null)
    }

    @Test
    fun testCreateSetPushTokenPayload() {
        val payload = RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN)
        payload shouldBe mapOf(
                "pushToken" to PUSH_TOKEN
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateTrackDeviceInfoPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createTrackDeviceInfoPayload(null)
    }

    @Test
    fun testCreateTrackDeviceInfoPayload() {
        val payload = RequestPayloadUtils.createTrackDeviceInfoPayload(mockRequestContext)
        payload shouldBe mapOf(
                "platform" to PLATFORM,
                "applicationVersion" to APPLICATION_VERSION,
                "deviceModel" to DEVICE_MODEL,
                "osVersion" to OS_VERSION,
                "sdkVersion" to SDK_VERSION,
                "language" to LANGUAGE,
                "timezone" to TIMEZONE
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactPayload_contactFieldValue_mustNotBeNull() {
        RequestPayloadUtils.createSetContactPayload(null, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createSetContactPayload(CONTACT_FIELD_VALUE, null)
    }

    @Test
    fun testCreateSetContactPayload() {
        val payload = RequestPayloadUtils.createSetContactPayload(CONTACT_FIELD_VALUE, mockRequestContext)
        payload shouldBe mapOf(
                "contactFieldId" to CONTACT_FIELD_ID,
                "contactFieldValue" to CONTACT_FIELD_VALUE
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateCustomEventPayload_eventName_mustNotBeNull() {
        RequestPayloadUtils.createCustomEventPayload(null, emptyMap(), mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateCustomEventPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createCustomEventPayload(EVENT_NAME, emptyMap(), null)
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

    @Test(expected = IllegalArgumentException::class)
    fun testCreateInternalCustomEventPayload_eventName_mustNotBeNull() {
        RequestPayloadUtils.createInternalCustomEventPayload(null, emptyMap(), mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateInternalCustomEventPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createInternalCustomEventPayload(EVENT_NAME, emptyMap(), null)
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

}