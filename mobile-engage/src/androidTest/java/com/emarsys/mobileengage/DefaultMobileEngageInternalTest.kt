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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock


class DefaultMobileEngageInternalTest {

    private companion object {
        const val CONTACT_FIELD_ID = 999
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
        const val OTHER_CONTACT_FIELD_VALUE = "contactFieldValue_potato"
        const val OPEN_ID_TOKEN = "idToken"
        const val OTHER_OPEN_ID_TOKEN = "idToken_OTHER"
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
    private lateinit var mockRequestModelWithNullContactFieldValueAndNullContactFieldId: RequestModel
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
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactTokenStorage).thenReturn(mockContactTokenStorage)
            whenever(clientStateStorage).thenReturn(mockClientStateStorage)
            whenever(pushTokenStorage).thenReturn(mockPushTokenStorage)
        }

        mockRequestModel = mock(RequestModel::class.java)
        mockRequestModelWithNullContactFieldValue = mock(RequestModel::class.java)
        mockRequestModelWithNullContactFieldValueAndNullContactFieldId = mock(RequestModel::class.java)

        mockRequestModelFactory = mock(MobileEngageRequestModelFactory::class.java).apply {
            whenever(createSetContactRequest(CONTACT_FIELD_ID, null)).thenReturn(
                mockRequestModelWithNullContactFieldValue
            )
            whenever(createSetContactRequest(null, null)).thenReturn(
                mockRequestModelWithNullContactFieldValueAndNullContactFieldId
            )
            whenever(createSetContactRequest(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)).thenReturn(
                mockRequestModel
            )
            whenever(createSetPushTokenRequest(PUSH_TOKEN)).thenReturn(mockRequestModel)
            whenever(createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(
                mockRequestModel
            )
            whenever(createTrackDeviceInfoRequest()).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(
                mockRequestModel
            )
            whenever(createRemovePushTokenRequest()).thenReturn(mockRequestModel)
        }

        uiHandler = Handler(Looper.getMainLooper())

        mockCompletionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = DefaultMobileEngageInternal(
            mockRequestManager,
            mockRequestModelFactory,
            mockRequestContext,
            mockSession,
            mockSessionIdHolder
        )
    }

    @Test
    fun testSetContact() {
        mobileEngageInternal.setContact(
            CONTACT_FIELD_ID,
            CONTACT_FIELD_VALUE,
            mockCompletionListener
        )

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testSetAuthenticatedContact_completionListener_canBeNull() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify(mockRequestManager).submit(mockRequestModelWithNullContactFieldValue, null)
    }

    @Test
    fun testSetAuthenticatedContact_shouldStartNewSession() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify(mockSession).startSession()
    }

    @Test
    fun testSetAuthenticatedContact_shouldEndRunningSessionBeforeStartingANewOne() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        whenever(mockSessionIdHolder.sessionId).thenReturn("testSessionId")

        mobileEngageInternal.setContact(CONTACT_FIELD_ID, "newContactFieldValue", null)

        inOrder(mockSession).run {
            verify(mockSession).startSession()
            verify(mockSession).endSession()
            verify(mockSession).startSession()
        }
    }

    @Test
    fun testSetAuthenticatedContact_shouldNotCallEndSession_whenNoSessionWasStartedBefore() {
        whenever(mockSessionIdHolder.sessionId).thenReturn(null)
        inOrder(mockSession) {
            mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)
            verify(mockSession, times(0)).endSession()
            verify(mockSession).startSession()
        }

    }

    @Test
    fun testSetContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        whenever(mockRequestContext.contactFieldValue).thenReturn(CONTACT_FIELD_VALUE)
        inOrder(mockSession) {
            mobileEngageInternal.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, null)

            mobileEngageInternal.setContact(CONTACT_FIELD_ID, OTHER_CONTACT_FIELD_VALUE, null)

            Mockito.verify(mockSession, times(1)).startSession()
        }
    }

    @Test
    fun testSetAuthenticatedContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        whenever(mockRequestContext.openIdToken).thenReturn(OPEN_ID_TOKEN)

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OTHER_OPEN_ID_TOKEN, null)

        verify(mockSession, times(1)).startSession()
    }

    @Test
    fun testSetAuthenticatedContact_shouldSetIdToken_inRequestContext() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify(mockRequestContext).contactFieldValue = null
        verify(mockRequestContext).openIdToken = OPEN_ID_TOKEN
    }

    @Test
    fun testClearContact() {
        whenever(mockSessionIdHolder.sessionId).thenReturn("sessionid")
        mobileEngageInternal = spy(mobileEngageInternal)

        mobileEngageInternal.clearContact(mockCompletionListener)

        inOrder(mobileEngageInternal, mockRequestManager, mockSession).run {
            verify(mobileEngageInternal).clearContact(mockCompletionListener)
            verify(mobileEngageInternal).resetContext()
            verify(mockSession).endSession()
            verify(mockRequestManager).submit(
                mockRequestModelWithNullContactFieldValueAndNullContactFieldId,
                mockCompletionListener
            )
            verifyNoMoreInteractions(mobileEngageInternal)
            verify(mockSession).startSession()
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
        verify(mockPushTokenStorage).remove()
        verify(mockRequestContext).contactFieldValue = null
        verify(mockRequestContext).openIdToken = null
        verify(mockRequestContext).contactFieldId = null
    }
}
