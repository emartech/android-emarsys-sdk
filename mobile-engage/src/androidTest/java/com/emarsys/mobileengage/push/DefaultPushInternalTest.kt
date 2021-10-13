package com.emarsys.mobileengage.push

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeCompletionListener
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.util.concurrent.CountDownLatch

class DefaultPushInternalTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
        const val EVENT_NAME = "customEventName"
        const val MESSAGE_OPEN_EVENT_NAME = "push:click"
        const val SID = "+43c_lODSmXqCvdOz"

        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    private lateinit var pushInternal: DefaultPushInternal

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockNotificationEventHandlerProvider: EventHandlerProvider
    private lateinit var mockSilentMessageEventHandlerProvider: EventHandlerProvider
    private lateinit var mockNotificationInformationListenerProvider: NotificationInformationListenerProvider
    private lateinit var mockSilentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider
    private lateinit var mockContactFieldValueStorage: StringStorage
    private lateinit var mockRefreshTokenStorage: StringStorage
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockClientStateStorage: StringStorage
    private lateinit var mockPushTokenStorage: StringStorage

    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {

        mockContactFieldValueStorage = mock()
        mockRefreshTokenStorage = mock()
        mockContactTokenStorage = mock()
        mockClientStateStorage = mock()
        mockPushTokenStorage = mock()
        mockNotificationInformationListenerProvider = mock()
        mockSilentNotificationInformationListenerProvider = mock()
        mockUuidProvider = mock {
            on { provideId() } doReturn REQUEST_ID
        }
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }

        mockRequestManager = mock()
        mockRequestContext = mock()

        mockRequestModel = mock()

        mockRequestModelFactory = mock {
            on { createSetPushTokenRequest(PUSH_TOKEN) } doReturn mockRequestModel
            on {
                createInternalCustomEventRequest(
                    EVENT_NAME,
                    EVENT_ATTRIBUTES
                )
            } doReturn mockRequestModel
            on { createRemovePushTokenRequest() } doReturn mockRequestModel
        }

        uiHandler = Handler(Looper.getMainLooper())

        mockCompletionListener = mock()
        mockEventServiceInternal = mock()
        mockNotificationEventHandlerProvider = mock()
        mockSilentMessageEventHandlerProvider = mock()
        pushInternal = DefaultPushInternal(
            mockRequestManager,
            uiHandler,
            mockRequestModelFactory,
            mockEventServiceInternal,
            mockPushTokenStorage,
            mockNotificationEventHandlerProvider,
            mockSilentMessageEventHandlerProvider,
            mockNotificationInformationListenerProvider,
            mockSilentNotificationInformationListenerProvider
        )
    }

    @Test
    fun testSetPushToken_whenRequest_success() {
        whenever(mockRequestManager.submit(eq(mockRequestModel), any())).thenAnswer {
            (it.arguments[1] as CompletionListener).onCompleted(null)
        }
        pushInternal.setPushToken(PUSH_TOKEN) {
            mockCompletionListener.onCompleted(null)
        }

        verify(mockRequestManager).submit(eq(mockRequestModel), any())
        verify(mockPushTokenStorage).set(PUSH_TOKEN)
        verify(mockCompletionListener).onCompleted(anyOrNull())
    }

    @Test
    fun testSetPushToken_whenRequest_failure_shouldNotStorePushToken() {
        val mockError: Throwable = mock()
        whenever(mockRequestManager.submit(eq(mockRequestModel), any())).thenAnswer {
            (it.arguments[1] as CompletionListener).onCompleted(mockError)
        }

        pushInternal.setPushToken(PUSH_TOKEN) {
            mockCompletionListener.onCompleted(mockError)
        }

        verify(mockRequestManager).submit(eq(mockRequestModel), any())
        verify(mockCompletionListener).onCompleted(mockError)
        verify(mockPushTokenStorage).get()
        verifyNoMoreInteractions(mockPushTokenStorage)
    }

    @Test
    fun testSetPushToken_doNotSendWhenPushTokenIsTheSame() {
        whenever(mockPushTokenStorage.get()).thenReturn(PUSH_TOKEN)

        pushInternal.setPushToken(PUSH_TOKEN, mockCompletionListener)

        verifyZeroInteractions(mockRequestModelFactory)
        verifyZeroInteractions(mockRequestManager)
    }

    @Test
    fun testSetPushToken_shouldCallCompletionListener_onMainThread() {
        val threadSpy: ThreadSpy<CompletionListener> = ThreadSpy()
        whenever(mockPushTokenStorage.get()).thenReturn(PUSH_TOKEN)
        whenever(mockCompletionListener.onCompleted(anyOrNull())).doAnswer(threadSpy)

        pushInternal.setPushToken(PUSH_TOKEN, mockCompletionListener)

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testGetPushToken_shouldGetPushTokenFromStorage() {
        whenever(mockPushTokenStorage.get()).thenReturn(PUSH_TOKEN)

        val result = pushInternal.pushToken

        verify(mockPushTokenStorage).get()
        result shouldBe PUSH_TOKEN
    }

    @Test
    fun testClearPushToken() {
        pushInternal.clearPushToken(mockCompletionListener)

        verify(mockPushTokenStorage).remove()
        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testRemovePushToken_completionListener_canBeNull() {
        pushInternal.clearPushToken(null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test
    fun testGetMessageId_shouldReturnNull_withEmptyIntent() {
        val result = pushInternal.getMessageId(Intent())
        result shouldBe null
    }

    @Test
    fun testGetMessageId_shouldReturnNull_withMissingUParam() {
        val bundlePayload = Bundle().apply {
            putString("key1", "value1")
        }

        val intent = Intent().apply {
            putExtra("payload", bundlePayload)
        }

        val result = pushInternal.getMessageId(intent)
        result shouldBe null
    }

    @Test
    fun testGetMessageId_shouldReturnNull_withMissingSIDParam() {
        val bundlePayload = Bundle().apply {
            putString("key1", "value1")
            putString("u", "{}")
        }

        val intent = Intent().apply {
            putExtra("payload", bundlePayload)
        }

        val result = pushInternal.getMessageId(intent)
        result shouldBe null
    }

    @Test
    fun testGetMessageId_shouldReturnNull_withInvalidJson() {

        val bundlePayload = Bundle().apply {
            putString("key1", "value1")
            putString("u", "{invalidJson}")
        }

        val intent = Intent().apply {
            putExtra("payload", bundlePayload)
        }

        val result = pushInternal.getMessageId(intent)

        result shouldBe null
    }

    @Test
    fun testGetMessageId_shouldReturnTheCorrectSIDValue() {
        val intent = createTestIntent()
        val result = pushInternal.getMessageId(intent)

        result shouldBe SID
    }

    @Test
    fun testTrackMessageOpen() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        whenever(
            mockRequestModelFactory.createInternalCustomEventRequest(
                MESSAGE_OPEN_EVENT_NAME,
                attributes
            )
        ).thenReturn(mockRequestModel)

        pushInternal.trackMessageOpen(createTestIntent(), mockCompletionListener)

        verify(mockEventServiceInternal).trackInternalCustomEventAsync(
            MESSAGE_OPEN_EVENT_NAME,
            attributes,
            mockCompletionListener
        )
    }

    @Test
    fun testTrackMessageOpen_completionListener_canBeNull() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        whenever(
            mockRequestModelFactory.createInternalCustomEventRequest(
                MESSAGE_OPEN_EVENT_NAME,
                attributes
            )
        ).thenReturn(mockRequestModel)

        pushInternal.trackMessageOpen(createTestIntent(), null)

        verify(mockEventServiceInternal).trackInternalCustomEventAsync(
            MESSAGE_OPEN_EVENT_NAME,
            attributes,
            null
        )
    }

    @Test
    fun testTrackMessageOpen_completionListener_canBeNull_sidIsNull() {
        pushInternal.trackMessageOpen(createBadTestIntent(), null)
    }

    @Test
    fun testTrackMessageOpen_shouldCallCompletionListenerWithError_whenMessageIdNotFound() {
        val completionListener: CompletionListener = mock()
        val countDownLatch = CountDownLatch(1)
        val fakeCompletionListener = FakeCompletionListener(countDownLatch, completionListener)

        pushInternal.trackMessageOpen(createBadTestIntent(), fakeCompletionListener)

        countDownLatch.await()

        argumentCaptor<IllegalArgumentException>().apply {
            verify(completionListener).onCompleted(capture())
            firstValue.message shouldBe "No messageId found!"
            firstValue.shouldBeTypeOf<IllegalArgumentException>()
        }
    }

    @Test
    fun testTrackMessageOpen_withEmptyIntent_shouldCallCompletionListener_onMainThread() {
        val completionListener: CompletionListener = mock()
        val threadSpy: ThreadSpy<CompletionListener> = ThreadSpy()
        whenever(completionListener.onCompleted(any())).doAnswer(threadSpy)

        pushInternal.trackMessageOpen(createBadTestIntent(), completionListener)

        threadSpy.verifyCalledOnMainThread()
    }

    private fun createTestIntent(): Intent {
        val bundlePayload = Bundle().apply {
            putString("key1", "value1")
            putString("u", """{"sid": "$SID"}""")
        }

        return Intent().apply {
            putExtra("payload", bundlePayload)
        }
    }

    private fun createBadTestIntent(): Intent {
        val bundlePayload = Bundle().apply {
            putString("key1", "value1")
            putString("u", "")
        }

        return Intent().apply {
            putExtra("payload", bundlePayload)
        }
    }

    @Test
    fun testSetNotificationEventHandler_shouldSetInProvider() {
        val mockEventHandler: EventHandler = mock()
        pushInternal.setNotificationEventHandler(mockEventHandler)

        verify(mockNotificationEventHandlerProvider).eventHandler = mockEventHandler
    }

    @Test
    fun testSetSilentMessageEventHandler_shouldSetInProvider() {
        val mockEventHandler: EventHandler = mock()
        pushInternal.setSilentMessageEventHandler(mockEventHandler)

        verify(mockSilentMessageEventHandlerProvider).eventHandler = mockEventHandler
    }

    @Test
    fun testSetNotificationInformationListener() {
        val mockNotificationInformationListener: NotificationInformationListener = mock()
        pushInternal.setNotificationInformationListener(mockNotificationInformationListener)

        verify(mockNotificationInformationListenerProvider).notificationInformationListener =
            mockNotificationInformationListener
    }

    @Test
    fun testSetSilentNotificationInformationListener() {
        val mockSilentNotificationInformationListener: NotificationInformationListener = mock()
        pushInternal.setSilentNotificationInformationListener(
            mockSilentNotificationInformationListener
        )

        verify(mockSilentNotificationInformationListenerProvider).silentNotificationInformationListener =
            mockSilentNotificationInformationListener
    }
}