package com.emarsys.mobileengage

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

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class DefaultMobileEngageInternalTest : AnnotationSpec() {

    private companion object {
        const val CONTACT_FIELD_ID = 999
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val CLIENT_ID = "clientId"
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
        whenever(mockSession.endSession(any())).doAnswer {
            (it.arguments[0] as CompletionListener).onCompleted(null)
        }

        mockSessionIdHolder = mock()

        mockUuidProvider = mock {
            on { provideId() }.thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock {
            on { provideTimestamp() }.thenReturn(TIMESTAMP)
        }

        mockDeviceInfo = mock {
            on { clientId }.thenReturn(CLIENT_ID)
            on { platform }.thenReturn(PLATFORM)
            on { applicationVersion }.thenReturn(APPLICATION_VERSION)
            on { model }.thenReturn(DEVICE_MODEL)
            on { osVersion }.thenReturn(OS_VERSION)
            on { sdkVersion }.thenReturn(SDK_VERSION)
            on { language }.thenReturn(LANGUAGE)
            on { timezone }.thenReturn(TIMEZONE)
        }

        mockRequestManager = mock()
        mockRequestContext = mock {
            on { timestampProvider }.thenReturn(mockTimestampProvider)
            on { uuidProvider }.thenReturn(mockUuidProvider)
            on { deviceInfo }.thenReturn(mockDeviceInfo)
            on { applicationCode }.thenReturn(APPLICATION_CODE)
            on { refreshTokenStorage }.thenReturn(mockRefreshTokenStorage)
            on { contactTokenStorage }.thenReturn(mockContactTokenStorage)
            on { clientStateStorage }.thenReturn(mockClientStateStorage)
            on { pushTokenStorage }.thenReturn(mockPushTokenStorage)
        }

        mockRequestModel = mock()
        mockRequestModelWithNullContactFieldValue = mock()
        mockRequestModelWithNullContactFieldValueAndNullContactFieldId = mock()

        mockRequestModelFactory = mock {
            on { createSetContactRequest(CONTACT_FIELD_ID, null) }.thenReturn(
                mockRequestModelWithNullContactFieldValue
            )
            on { createSetContactRequest(null, null) }.thenReturn(
                mockRequestModelWithNullContactFieldValueAndNullContactFieldId
            )
            on { createSetContactRequest(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) }.thenReturn(
                mockRequestModel
            )
            on { createSetPushTokenRequest(PUSH_TOKEN) }.thenReturn(mockRequestModel)
            on { createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES) }.thenReturn(
                mockRequestModel
            )
            on { createTrackDeviceInfoRequest() }.thenReturn(mockRequestModel)
            on { createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES) }.thenReturn(
                mockRequestModel
            )
            on { createRemovePushTokenRequest() }.thenReturn(mockRequestModel)
        }

        mockCompletionListener = mock()

        mobileEngageInternal = DefaultMobileEngageInternal(
            mockRequestManager,
            mockRequestModelFactory,
            mockRequestContext,
            mockSession,
            mockSessionIdHolder
        )
    }

    @Test
    fun testSetContact_shouldCallRequestManager_whenNewSessionIsNeeded() {
        whenever(mockRequestContext.contactFieldValue).thenReturn(OTHER_CONTACT_FIELD_VALUE)

        mobileEngageInternal.setContact(
            CONTACT_FIELD_ID,
            CONTACT_FIELD_VALUE,
            mockCompletionListener
        )

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testSetContact_shouldNotCallRequestManager_whenSessionIsNotChanging() {
        whenever(mockRequestContext.contactFieldValue).thenReturn(CONTACT_FIELD_VALUE)

        mobileEngageInternal.setContact(
            CONTACT_FIELD_ID,
            CONTACT_FIELD_VALUE,
            mockCompletionListener
        )

        verifyNoInteractions(mockRequestManager)
        verify(mockCompletionListener).onCompleted(null)
    }

    @Test
    fun testSetAuthenticatedContact_completionListener_canBeNull() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify(mockRequestManager).submit(mockRequestModelWithNullContactFieldValue, null)
    }

    @Test
    fun testSetAuthenticatedContact_shouldStartNewSession() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify(mockSession).startSession(any())
    }

    @Test
    fun testSetAuthenticatedContact_shouldEndRunningSessionBeforeStartingANewOne() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        whenever(mockSessionIdHolder.sessionId).thenReturn("testSessionId")

        mobileEngageInternal.setContact(CONTACT_FIELD_ID, "newContactFieldValue", null)

        inOrder(mockSession).run {
            verify(mockSession).startSession(any())
            verify(mockSession).endSession(any())
            verify(mockSession).startSession(any())
        }
    }

    @Test
    fun testSetAuthenticatedContact_shouldNotCallEndSession_whenNoSessionWasStartedBefore() {
        whenever(mockSessionIdHolder.sessionId).thenReturn(null)
        inOrder(mockSession) {
            mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)
            verify(mockSession, times(0)).endSession(any())
            verify(mockSession).startSession(any())
        }

    }

    @Test
    fun testSetContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        whenever(mockRequestContext.contactFieldValue).thenReturn(CONTACT_FIELD_VALUE)
        inOrder(mockSession) {
            mobileEngageInternal.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, null)

            mobileEngageInternal.setContact(CONTACT_FIELD_ID, OTHER_CONTACT_FIELD_VALUE, null)

            verify(mockSession, times(1)).startSession(any())
        }
    }

    @Test
    fun testSetAuthenticatedContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        whenever(mockRequestContext.openIdToken).thenReturn(OPEN_ID_TOKEN)

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OTHER_OPEN_ID_TOKEN, null)

        verify(mockSession, times(1)).startSession(any())
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
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }

        whenever(mockRequestManager.submit(any(), any())).doAnswer {
            (it.arguments[1] as CompletionListener).onCompleted(null)
        }
        mobileEngageInternal.clearContact(completionListener)

        latch.await(2000L, TimeUnit.MILLISECONDS)
        inOrder(mobileEngageInternal, mockRequestManager, mockSession).run {
            verify(mobileEngageInternal).clearContact(completionListener)
            verify(mockSession).endSession(any())
            verify(mobileEngageInternal).doClearContact(any())
            verify(mobileEngageInternal).resetContext()
            verify(mobileEngageInternal).doSetContact(
                isNull(),
                isNull(),
                isNull(),
                any<CompletionListener>()
            )
            verify(mockRequestManager).submit(
                eq(mockRequestModelWithNullContactFieldValueAndNullContactFieldId),
                any()
            )
            verifyNoMoreInteractions(mobileEngageInternal)
            verify(mockSession).startSession(any())
        }
    }

    @Test
    fun testClearContact_shouldEndCurrentSession() {
        whenever(mockSessionIdHolder.sessionId).thenReturn("testSessionId")
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(true)
        whenever(mockRequestContext.contactTokenStorage.get()).thenReturn("contactToken")
        mobileEngageInternal.clearContact(null)

        verify(mockSession).endSession(any())
    }

    @Test
    fun testClearContact_shouldCallOnCompleted_whenContactWasAlreadyAnonymous() {
        whenever(mockRequestContext.hasContactIdentification()).thenReturn(false)
        whenever(mockRequestContext.contactTokenStorage.get()).thenReturn("contactToken")
        whenever(mockSessionIdHolder.sessionId).thenReturn("testSessionId")

        var result = false
        mobileEngageInternal.clearContact {
            result = true
        }
        result shouldBe true
        verifyNoInteractions(mockSession)
        verifyNoInteractions(mockRequestManager)
    }

    @Test
    fun testClearContact_shouldNotCallEndCurrentSession_whenThereWasNoSessionInProgress() {
        whenever(mockSessionIdHolder.sessionId).thenReturn(null)

        mobileEngageInternal.clearContact(null)

        verify(mockSession, times(0)).endSession(any())
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
