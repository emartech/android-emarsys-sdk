package com.emarsys.mobileengage.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.RequestContext
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.mobileengage.util.RequestUrlUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class RequestModelFactoryTest{

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
    }

    lateinit var mockRequestContext: RequestContext
    lateinit var mockTimestampProvider: TimestampProvider
    lateinit var mockUuidProvider: UUIDProvider
    lateinit var mockDeviceInfo: DeviceInfo
    lateinit var requestFactory: RequestModelFactory

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockUuidProvider = Mockito.mock(UUIDProvider::class.java).apply {
            MockitoTestUtils.whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = Mockito.mock(TimestampProvider::class.java).apply {
            MockitoTestUtils.whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        mockDeviceInfo = Mockito.mock(DeviceInfo::class.java).apply {
            MockitoTestUtils.whenever(hwid).thenReturn(HARDWARE_ID)
        }

        mockRequestContext = Mockito.mock(RequestContext::class.java).apply {
            MockitoTestUtils.whenever(timestampProvider).thenReturn(mockTimestampProvider)
            MockitoTestUtils.whenever(uuidProvider).thenReturn(mockUuidProvider)
            MockitoTestUtils.whenever(deviceInfo).thenReturn(mockDeviceInfo)
            MockitoTestUtils.whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }

        requestFactory = RequestModelFactory(mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        RequestModelFactory(null)
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

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactRequest_contactFieldValue_mustNotBeNull() {
        requestFactory.createSetContactRequest(null)
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
}