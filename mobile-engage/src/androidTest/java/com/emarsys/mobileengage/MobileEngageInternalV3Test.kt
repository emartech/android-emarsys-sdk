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
    }

    lateinit var mobileEngageInternal: MobileEngageInternalV3

    lateinit var requestManagerMock: RequestManager
    lateinit var requestContextMock: RequestContext
    lateinit var completionListener: CompletionListener

    lateinit var timestampProviderMock: TimestampProvider
    lateinit var uuidProviderMock: UUIDProvider
    lateinit var deviceInfoMock: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        uuidProviderMock = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        timestampProviderMock = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        deviceInfoMock = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
            whenever(platform).thenReturn(PLATFORM)
            whenever(applicationVersion).thenReturn(APPLICATION_VERSION)
            whenever(model).thenReturn(DEVICE_MODEL)
            whenever(osVersion).thenReturn(OS_VERSION)
            whenever(sdkVersion).thenReturn(SDK_VERSION)
            whenever(language).thenReturn(LANGUAGE)
            whenever(timezone).thenReturn(TIMEZONE)
        }

        requestManagerMock = mock(RequestManager::class.java)
        requestContextMock = mock(RequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(timestampProviderMock)
            whenever(uuidProvider).thenReturn(uuidProviderMock)
            whenever(deviceInfo).thenReturn(deviceInfoMock)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }

        completionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = MobileEngageInternalV3(requestManagerMock, requestContextMock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        MobileEngageInternalV3(null, requestContextMock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageInternalV3(requestManagerMock, null)
    }

    @Test
    fun testSetPushToken() {
        val expectedRequestModel = RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, requestContextMock)

        mobileEngageInternal.setPushToken(PUSH_TOKEN, completionListener)

        verify(requestManagerMock).submit(expectedRequestModel, completionListener)
    }

    @Test
    fun testSetPushToken_completionListener_canBeNull() {
        val expectedRequestModel = RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, requestContextMock)

        mobileEngageInternal.setPushToken(PUSH_TOKEN, null)

        verify(requestManagerMock).submit(expectedRequestModel, null)
    }

    @Test
    fun testSetPushToken_whenPushTokenIsNull_callShouldBeIgnored() {
        mobileEngageInternal.setPushToken(null, completionListener)

        verifyZeroInteractions(requestManagerMock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetContact_contactFieldValue_mustNotBeNull() {
        mobileEngageInternal.setContact(null, completionListener)
    }

    @Test
    fun testSetContact() {
        val expectedRequestModel = RequestModelUtils.createSetContactRequest(CONTACT_FIELD_VALUE, requestContextMock)

        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, completionListener)

        verify(requestManagerMock).submit(expectedRequestModel, completionListener)
    }

    @Test
    fun testSetContact_completionListener_canBeNull() {
        val expectedRequestModel = RequestModelUtils.createSetContactRequest(CONTACT_FIELD_VALUE, requestContextMock)

        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, null)

        verify(requestManagerMock).submit(expectedRequestModel, null)
    }

    @Test
    fun testTrackDeviceInfo() {
        val expectedRequestModel = RequestModelUtils.createTrackDeviceInfoRequest(requestContextMock)

        mobileEngageInternal.trackDeviceInfo()

        verify(requestManagerMock).submit(expectedRequestModel, null)
    }
}
