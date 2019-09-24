package com.emarsys.mobileengage

import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*


class DefaultMobileEngageInternalTest {

    private companion object {
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
        const val EVENT_NAME = "customEventName"
        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    private lateinit var mobileEngageInternal: DefaultMobileEngageInternal

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockContactFieldValueStorage: Storage<String>
    private lateinit var mockRefreshTokenStorage: Storage<String>
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var mockClientStateStorage: Storage<String>

    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockEventServiceInternal = mock(EventServiceInternal::class.java)
        mockPushInternal = mock(PushInternal::class.java)

        mockContactFieldValueStorage = mock(Storage::class.java) as Storage<String>
        mockRefreshTokenStorage = mock(Storage::class.java) as Storage<String>
        mockContactTokenStorage = mock(Storage::class.java) as Storage<String>
        mockClientStateStorage = mock(Storage::class.java) as Storage<String>

        val mockApplicationCodeStorage = (mock(Storage::class.java) as Storage<String?>).apply {
            whenever(get()).thenReturn(APPLICATION_CODE)
        }

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
        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCodeStorage).thenReturn(mockApplicationCodeStorage)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactTokenStorage).thenReturn(mockContactTokenStorage)
            whenever(clientStateStorage).thenReturn(mockClientStateStorage)
        }

        mockRequestModel = mock(RequestModel::class.java)

        mockRequestModelFactory = mock(MobileEngageRequestModelFactory::class.java).apply {
            whenever(createSetContactRequest(CONTACT_FIELD_VALUE)).thenReturn(mockRequestModel)
            whenever(createSetPushTokenRequest(PUSH_TOKEN)).thenReturn(mockRequestModel)
            whenever(createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createTrackDeviceInfoRequest()).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createRemovePushTokenRequest()).thenReturn(mockRequestModel)
        }

        uiHandler = Handler(Looper.getMainLooper())

        mockCompletionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = DefaultMobileEngageInternal(mockRequestManager, mockRequestModelFactory, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        DefaultMobileEngageInternal(null, mockRequestModelFactory, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        DefaultMobileEngageInternal(mockRequestManager, null, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        DefaultMobileEngageInternal(mockRequestManager, mockRequestModelFactory, null)
    }

    @Test
    fun testSetContact() {
        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testSetContact_shouldSetContactFieldValue_toContactFieldValueStorage() {
        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, mockCompletionListener)

        verify(mockContactFieldValueStorage).set(CONTACT_FIELD_VALUE)
    }

    @Test
    fun testSetContact_completionListener_canBeNull() {
        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test
    fun testClearContact() {
        mobileEngageInternal = spy(mobileEngageInternal)

        mobileEngageInternal.clearContact(mockCompletionListener)

        inOrder(mobileEngageInternal).run {
            verify(mobileEngageInternal).clearContact(mockCompletionListener)
            verify(mobileEngageInternal).resetContext()
            verify(mobileEngageInternal).setContact(null, mockCompletionListener)
            verifyNoMoreInteractions(mobileEngageInternal)
        }
    }

    @Test
    fun testResetContext_shouldClearTokenStorages() {
        mobileEngageInternal.resetContext()

        verify(mockRefreshTokenStorage).remove()
        verify(mockContactTokenStorage).remove()
        verify(mockContactFieldValueStorage).remove()
    }


}
