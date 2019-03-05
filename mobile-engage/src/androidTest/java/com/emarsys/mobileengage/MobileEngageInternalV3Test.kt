package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.mobileengage.util.RequestModelUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*


class MobileEngageInternalV3Test {

    companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
        const val PLATFORM = "android"
        const val APPLICATION_VERSION = "1.0.2"
        const val DEVICE_MODEL = "GT-9100"
        const val OS_VERSION = "9.0"
        const val SDK_VERSION = "1.7.2"
        const val LANGUAGE = "en-US"
        const val TIMEZONE = "+0200"

        const val CONTACT_FIELD_VALUE = "contactFieldValue"
        const val CONTACT_FIELD_ID = 3
        const val EVENT_NAME = "customEventName"
        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    lateinit var mobileEngageInternal: MobileEngageInternalV3

    lateinit var mockRequestManager: RequestManager
    lateinit var mockRequestContext: RequestContext
    lateinit var completionListener: CompletionListener

    lateinit var mockTimestampProvider: TimestampProvider
    lateinit var mockUuidProvider: UUIDProvider
    lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
            whenever(platform).thenReturn(PLATFORM)
            whenever(applicationVersion).thenReturn(APPLICATION_VERSION)
            whenever(model).thenReturn(DEVICE_MODEL)
            whenever(osVersion).thenReturn(OS_VERSION)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
            whenever(language).thenReturn(LANGUAGE)
            whenever(timezone).thenReturn(TIMEZONE)
        }

        mockRequestManager = mock(RequestManager::class.java)
        mockRequestContext = mock(RequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }

        completionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = MobileEngageInternalV3(mockRequestManager, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        MobileEngageInternalV3(null, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageInternalV3(mockRequestManager, null)
    }

    @Test
    fun testSetPushToken() {
        val expectedRequestModel = RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, mockRequestContext)

        mobileEngageInternal.setPushToken(PUSH_TOKEN, completionListener)

        verify(mockRequestManager).submit(expectedRequestModel, completionListener)
    }

    @Test
    fun testSetPushToken_completionListener_canBeNull() {
        val expectedRequestModel = RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, mockRequestContext)

        mobileEngageInternal.setPushToken(PUSH_TOKEN, null)

        verify(mockRequestManager).submit(expectedRequestModel, null)
    }

    @Test
    fun testSetPushToken_whenPushTokenIsNull_callShouldBeIgnored() {
        mobileEngageInternal.setPushToken(null, completionListener)

        verifyZeroInteractions(mockRequestManager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetContact_contactFieldValue_mustNotBeNull() {
        mobileEngageInternal.setContact(null, completionListener)
    }

    @Test
    fun testSetContact() {
        val expectedRequestModel = RequestModelUtils.createSetContactRequest(CONTACT_FIELD_VALUE, mockRequestContext)

        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, completionListener)

        verify(mockRequestManager).submit(expectedRequestModel, completionListener)
    }

    @Test
    fun testSetContact_completionListener_canBeNull() {
        val expectedRequestModel = RequestModelUtils.createSetContactRequest(CONTACT_FIELD_VALUE, mockRequestContext)

        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, null)

        verify(mockRequestManager).submit(expectedRequestModel, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCustomEvent_eventName_mustNotBeNull() {
        mobileEngageInternal.trackCustomEvent(null, emptyMap(), completionListener)
    }

    @Test
    fun testTrackCustomEvent() {

        val expectedRequestModel = RequestModelUtils.createTrackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockRequestContext)

        mobileEngageInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, completionListener)

        verify(mockRequestManager).submit(expectedRequestModel, completionListener)
    }

    @Test
    fun testTrackCustomEvent_completionListener_canBeNull() {

        val expectedRequestModel = RequestModelUtils.createTrackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockRequestContext)

        mobileEngageInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)

        verify(mockRequestManager).submit(expectedRequestModel, null)
    }

    @Test
    fun testTrackDeviceInfo() {
        val expectedRequestModel = RequestModelUtils.createTrackDeviceInfoRequest(mockRequestContext)

        mobileEngageInternal.trackDeviceInfo()

        verify(mockRequestManager).submit(expectedRequestModel, null)
    }
}
