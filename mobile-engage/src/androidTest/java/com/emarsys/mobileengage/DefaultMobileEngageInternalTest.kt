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
import com.emarsys.testUtil.KotestRunnerAndroid
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(KotestRunnerAndroid::class)
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
    fun setUp() {
        mockEventServiceInternal = mockk(relaxed = true)
        mockPushInternal = mockk(relaxed = true)

        mockRefreshTokenStorage = mockk(relaxed = true)
        mockContactTokenStorage = mockk(relaxed = true)
        mockClientStateStorage = mockk(relaxed = true)
        mockPushTokenStorage = mockk(relaxed = true)
        mockSession = mockk(relaxed = true)
        every { (mockSession.endSession(any())) } answers {
            (args[0] as CompletionListener).onCompleted(null)
        }

        mockSessionIdHolder = mockk(relaxed = true)

        mockUuidProvider = mockk(relaxed = true)
        every { mockUuidProvider.provideId() } returns REQUEST_ID

        mockTimestampProvider = mockk(relaxed = true)
        every { mockTimestampProvider.provideTimestamp() } returns TIMESTAMP

        mockDeviceInfo = mockk(relaxed = true)
        every { mockDeviceInfo.clientId } returns CLIENT_ID
        every { mockDeviceInfo.platform } returns PLATFORM
        every { mockDeviceInfo.applicationVersion } returns APPLICATION_VERSION
        every { mockDeviceInfo.model } returns DEVICE_MODEL
        every { mockDeviceInfo.osVersion } returns OS_VERSION
        every { mockDeviceInfo.sdkVersion } returns SDK_VERSION
        every { mockDeviceInfo.language } returns LANGUAGE
        every { mockDeviceInfo.timezone } returns TIMEZONE

        mockRequestManager = mockk(relaxed = true)
        mockRequestContext = mockk(relaxed = true)
        every { mockRequestContext.timestampProvider } returns mockTimestampProvider
        every { mockRequestContext.uuidProvider } returns mockUuidProvider
        every { mockRequestContext.deviceInfo } returns mockDeviceInfo
        every { mockRequestContext.applicationCode } returns APPLICATION_CODE
        every { mockRequestContext.refreshTokenStorage } returns mockRefreshTokenStorage
        every { mockRequestContext.contactTokenStorage } returns mockContactTokenStorage
        every { mockRequestContext.clientStateStorage } returns mockClientStateStorage
        every { mockRequestContext.pushTokenStorage } returns mockPushTokenStorage

        mockRequestModel = mockk(relaxed = true)
        mockRequestModelWithNullContactFieldValue = mockk(relaxed = true)
        mockRequestModelWithNullContactFieldValueAndNullContactFieldId = mockk(relaxed = true)

        mockRequestModelFactory = mockk(relaxed = true)
        every { mockRequestModelFactory.createSetContactRequest(CONTACT_FIELD_ID, null) } returns
                mockRequestModelWithNullContactFieldValue
        every { mockRequestModelFactory.createSetContactRequest(null, null) } returns
                mockRequestModelWithNullContactFieldValueAndNullContactFieldId

        every {
            mockRequestModelFactory.createSetContactRequest(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)
        } returns
                mockRequestModel

        every { mockRequestModelFactory.createSetPushTokenRequest(PUSH_TOKEN) } returns mockRequestModel
        every {
            mockRequestModelFactory.createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)
        } returns mockRequestModel

        every { mockRequestModelFactory.createTrackDeviceInfoRequest() } returns mockRequestModel
        every {
            mockRequestModelFactory.createInternalCustomEventRequest(
                EVENT_NAME,
                EVENT_ATTRIBUTES
            )
        } returns
                mockRequestModel

        every { mockRequestModelFactory.createRemovePushTokenRequest() } returns mockRequestModel

        mockCompletionListener = mockk(relaxed = true)

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
        every { mockRequestContext.contactFieldValue } returns OTHER_CONTACT_FIELD_VALUE

        mobileEngageInternal.setContact(
            CONTACT_FIELD_ID,
            CONTACT_FIELD_VALUE,
            mockCompletionListener
        )

        verify { mockRequestManager.submit(mockRequestModel, mockCompletionListener) }
    }

    @Test
    fun testSetContact_shouldNotCallRequestManager_whenSessionIsNotChanging() {
        every { mockRequestContext.contactFieldValue } returns CONTACT_FIELD_VALUE

        mobileEngageInternal.setContact(
            CONTACT_FIELD_ID,
            CONTACT_FIELD_VALUE,
            mockCompletionListener
        )

        confirmVerified(mockRequestManager)
        verify { mockCompletionListener.onCompleted(null) }
    }

    @Test
    fun testSetAuthenticatedContact_completionListener_canBeNull() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify { mockRequestManager.submit(mockRequestModelWithNullContactFieldValue, null) }
    }

    @Test
    fun testSetAuthenticatedContact_shouldStartNewSession() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify { mockSession.startSession(any()) }
    }

    @Test
    fun testSetAuthenticatedContact_shouldEndRunningSessionBeforeStartingANewOne() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        every { mockSessionIdHolder.sessionId } returns "testSessionId"

        mobileEngageInternal.setContact(CONTACT_FIELD_ID, "newContactFieldValue", null)

        verifyOrder {
            mockSession.startSession(any())
            mockSession.endSession(any())
            mockSession.startSession(any())
        }
    }

    @Test
    fun testSetAuthenticatedContact_shouldNotCallEndSession_whenNoSessionWasStartedBefore() {
        every { mockSessionIdHolder.sessionId } returns null

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify { mockSession.startSession(any()) }
        confirmVerified(mockSession)
    }

    @Test
    fun testSetContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        every { mockRequestContext.contactFieldValue } returns CONTACT_FIELD_VALUE

        mobileEngageInternal.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, null)

        mobileEngageInternal.setContact(CONTACT_FIELD_ID, OTHER_CONTACT_FIELD_VALUE, null)

        verify(exactly = 1) {
            mockSession.startSession(any())
        }
    }

    @Test
    fun testSetAuthenticatedContact_shouldStartNewSession_onlyWhenItIsDifferentFromPreviousContact() {
        every { mockRequestContext.openIdToken } returns OPEN_ID_TOKEN

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OTHER_OPEN_ID_TOKEN, null)

        verify(exactly = 1) { mockSession.startSession(any()) }
    }

    @Test
    fun testSetAuthenticatedContact_shouldSetIdToken_inRequestContext() {
        mobileEngageInternal.setAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN, null)

        verify { mockRequestContext.contactFieldValue = null }
        verify { mockRequestContext.openIdToken = OPEN_ID_TOKEN }
    }

    @Test
    fun testClearContact() {
        every { mockSessionIdHolder.sessionId } returns "sessionId"
        every { mockContactTokenStorage.get() } returns "contactToken"
        every { mockRequestContext.hasContactIdentification() } returns true

        mobileEngageInternal = spyk(mobileEngageInternal)
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }

        every { mockRequestManager.submit(any(), any()) } answers {
            (args[1] as CompletionListener).onCompleted(null)
        }

        mobileEngageInternal.clearContact(completionListener)

        latch.await(2000L, TimeUnit.MILLISECONDS)
        verifyOrder {
            mobileEngageInternal.clearContact(completionListener)
            mockSession.endSession(any())
            mobileEngageInternal.doClearContact(any())
            mobileEngageInternal.resetContext()
            mobileEngageInternal.doSetContact(
                null,
                null,
                null,
                any()
            )
            mockRequestManager.submit(
                mockRequestModelWithNullContactFieldValueAndNullContactFieldId,
                any()
            )
            mockSession.startSession(any())
        }
        confirmVerified(mobileEngageInternal)
    }

    @Test
    fun testClearContact_shouldEndCurrentSession() {
        every { mockSessionIdHolder.sessionId } returns "testSessionId"
        every { mockRequestContext.hasContactIdentification() } returns true
        every { mockRequestContext.contactTokenStorage.get() } returns "contactToken"

        mobileEngageInternal.clearContact(null)

        verify { mockSession.endSession(any()) }
    }

    @Test
    fun testClearContact_shouldCallOnCompleted_whenContactWasAlreadyAnonymous() {
        every { mockRequestContext.hasContactIdentification() } returns false
        every { mockRequestContext.contactTokenStorage.get() } returns "contactToken"
        every { mockSessionIdHolder.sessionId } returns "testSessionId"

        var result = false
        mobileEngageInternal.clearContact {
            result = true
        }
        result shouldBe true
        confirmVerified(mockSession)
        confirmVerified(mockRequestManager)
    }

    @Test
    fun testClearContact_shouldNotCallEndCurrentSession_whenThereWasNoSessionInProgress() {
        every { mockRequestContext.contactTokenStorage.get() } returns "contactToken"
        every { mockSessionIdHolder.sessionId } returns "testSessionId"
        every { mockSessionIdHolder.sessionId } returns null

        mobileEngageInternal.clearContact(null)

        verify(exactly = 0) { mockSession.endSession(any()) }
    }

    @Test
    fun testResetContext_shouldClearTokenStorages() {
        mobileEngageInternal.resetContext()

        verify { mockRefreshTokenStorage.remove() }
        verify { mockContactTokenStorage.remove() }
        verify { mockPushTokenStorage.remove() }
        verify { mockRequestContext.contactFieldValue = null }
        verify { mockRequestContext.openIdToken = null }
        verify { mockRequestContext.contactFieldId = null }
    }
}
