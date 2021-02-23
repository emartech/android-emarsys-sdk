package com.emarsys.mobileengage

import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
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
        const val ID_TOKEN  = "idToken"
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
    private lateinit var mockRequestModelWithNullContactFieldValue: RequestModel
    private lateinit var mockContactFieldValueStorage: StringStorage
    private lateinit var mockRefreshTokenStorage: StringStorage
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockClientStateStorage: StringStorage
    private lateinit var mockPushTokenStorage: StringStorage
    private lateinit var mockSession: MobileEngageSession
    private lateinit var mockSessionIdHolder: SessionIdHolder

    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockEventServiceInternal = mock()
        mockPushInternal = mock()

        mockContactFieldValueStorage = mock()
        mockRefreshTokenStorage = mock()
        mockContactTokenStorage = mock()
        mockClientStateStorage = mock()
        mockPushTokenStorage = mock()
        mockSession = mock()
        mockSessionIdHolder = mock()

        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hardwareId).thenReturn(HARDWARE_ID)
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
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactTokenStorage).thenReturn(mockContactTokenStorage)
            whenever(clientStateStorage).thenReturn(mockClientStateStorage)
            whenever(pushTokenStorage).thenReturn(mockPushTokenStorage)
        }

        mockRequestModel = mock(RequestModel::class.java)
        mockRequestModelWithNullContactFieldValue = mock(RequestModel::class.java)

        mockRequestModelFactory = mock(MobileEngageRequestModelFactory::class.java).apply {
            whenever(createSetContactRequest(null)).thenReturn(mockRequestModelWithNullContactFieldValue)
            whenever(createSetContactRequest(CONTACT_FIELD_VALUE)).thenReturn(mockRequestModel)
            whenever(createSetPushTokenRequest(PUSH_TOKEN)).thenReturn(mockRequestModel)
            whenever(createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createTrackDeviceInfoRequest()).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createRemovePushTokenRequest()).thenReturn(mockRequestModel)
        }

        uiHandler = Handler(Looper.getMainLooper())

        mockCompletionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = DefaultMobileEngageInternal(mockRequestManager, mockRequestModelFactory, mockRequestContext, mockSession, mockSessionIdHolder)
    }

    @Test
    fun testSetContact() {
        mobileEngageInternal.setContact(CONTACT_FIELD_VALUE, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testSetContact_delegatesTo_setAuthorizedContact() {
        val spyMobileEngageInternal = spy(mobileEngageInternal)

        spyMobileEngageInternal.setContact(CONTACT_FIELD_VALUE, mockCompletionListener)

        verify(spyMobileEngageInternal).setAuthorizedContact(CONTACT_FIELD_VALUE, null, mockCompletionListener)
    }

    @Test
    fun testSetAuthorizedContact_shouldSetContactFieldValue_toContactFieldValueStorage() {
        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null, mockCompletionListener)

        verify(mockContactFieldValueStorage).set(CONTACT_FIELD_VALUE)
    }

    @Test
    fun testSetAuthorizedContact_completionListener_canBeNull() {
        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null,  null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test
    fun testSetAuthorizedContact_shouldStartNewSession() {
        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null, null)

        verify(mockSession).startSession()
    }

    @Test
    fun testSetAuthorizedContact_shouldEndRunningSessionBeforeStartingANewOne() {
        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null, null)

        whenever(mockSessionIdHolder.sessionId).thenReturn("testSessionId")

        mobileEngageInternal.setContact("newContactFieldValue", null)

        inOrder(mockSession).run {
            verify(mockSession).startSession()
            verify(mockSession).endSession()
            verify(mockSession).startSession()
        }
    }

    @Test
    fun testSetAuthorizedContact_shouldNotCallEndSession_whenNoSessionWasStartedBefore() {
        whenever(mockSessionIdHolder.sessionId).thenReturn(null)

        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null, null)

        verify(mockSession, times(0)).endSession()
        verify(mockSession).startSession()
    }

    @Test
    fun testSetAuthorizedContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null, null)

        val mockContactFieldValueStorage: StringStorage = mock {
            on { get() } doReturn CONTACT_FIELD_VALUE
        }
        whenever(mockRequestContext.contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)

        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, null, null)

        verify(mockSession, times(1)).startSession()
    }

    @Test
    fun testSetAuthorizedContact_shouldSetIdToken_inRequestContext() {
        mobileEngageInternal.setAuthorizedContact(CONTACT_FIELD_VALUE, ID_TOKEN, null)

        verify(mockContactFieldValueStorage).set(CONTACT_FIELD_VALUE)
        verify(mockRequestContext).openIdToken = ID_TOKEN
    }

    @Test
    fun testClearContact() {
        mobileEngageInternal = spy(mobileEngageInternal)

        mobileEngageInternal.clearContact(mockCompletionListener)

        inOrder(mobileEngageInternal, mockRequestManager).run {
            verify(mobileEngageInternal).clearContact(mockCompletionListener)
            verify(mobileEngageInternal).resetContext()
            verify(mockRequestManager).submit(mockRequestModelWithNullContactFieldValue, mockCompletionListener)
            verifyNoMoreInteractions(mobileEngageInternal)
        }
    }

    @Test
    fun testClearContact_shouldEndCurrentSession() {
        whenever(mockSessionIdHolder.sessionId).thenReturn("testSessionId")
        mobileEngageInternal.clearContact(null)

        verify(mockSession).endSession()
    }

    @Test
    fun testClearContact_shouldNotCallEndCurrentSession_whenThereWasNoSessionInProgress() {
        whenever(mockSessionIdHolder.sessionId).thenReturn(null)

        mobileEngageInternal.clearContact(null)

        verify(mockSession, times(0)).endSession()
    }

    @Test
    fun testResetContext_shouldClearTokenStorages() {
        mobileEngageInternal.resetContext()

        verify(mockRefreshTokenStorage).remove()
        verify(mockContactTokenStorage).remove()
        verify(mockContactFieldValueStorage).remove()
        verify(mockPushTokenStorage).remove()
    }
}
