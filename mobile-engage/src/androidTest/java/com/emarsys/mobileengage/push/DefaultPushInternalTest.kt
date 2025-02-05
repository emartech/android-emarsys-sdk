package com.emarsys.mobileengage.push

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeCompletionListener
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.CapturingSlot
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class DefaultPushInternalTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
        const val LOCAL_PUSH_TOKEN = "local_kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
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
    private lateinit var mockNotificationCacheableEventHandler: CacheableEventHandler
    private lateinit var mockSilentMessageCacheableEventHandler: CacheableEventHandler
    private lateinit var mockNotificationInformationListenerProvider: NotificationInformationListenerProvider
    private lateinit var mockSilentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider
    private lateinit var mockContactFieldValueStorage: StringStorage
    private lateinit var mockRefreshTokenStorage: StringStorage
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockClientStateStorage: StringStorage
    private lateinit var mockPushTokenStorage: StringStorage
    private lateinit var mockLocalPushTokenStorage: StringStorage

    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Before
    fun setUp() {
        mockContactFieldValueStorage = mockk(relaxed = true)
        mockRefreshTokenStorage = mockk(relaxed = true)
        mockContactTokenStorage = mockk(relaxed = true)
        mockClientStateStorage = mockk(relaxed = true)
        mockPushTokenStorage = mockk(relaxed = true)
        mockLocalPushTokenStorage = mockk(relaxed = true)
        mockNotificationInformationListenerProvider = mockk(relaxed = true)
        mockSilentNotificationInformationListenerProvider = mockk(relaxed = true)
        mockUuidProvider = mockk(relaxed = true)
        every { mockUuidProvider.provideId() } returns REQUEST_ID

        mockTimestampProvider = mockk(relaxed = true)
        every { mockTimestampProvider.provideTimestamp() } returns TIMESTAMP


        mockRequestManager = mockk(relaxed = true)
        mockRequestContext = mockk(relaxed = true)

        mockRequestModel = mockk(relaxed = true)

        mockRequestModelFactory = mockk(relaxed = true)
        every { mockRequestModelFactory.createSetPushTokenRequest(PUSH_TOKEN) } returns mockRequestModel
        every {
            mockRequestModelFactory.createInternalCustomEventRequest(
                EVENT_NAME,
                EVENT_ATTRIBUTES
            )
        } returns mockRequestModel
        every { mockRequestModelFactory.createRemovePushTokenRequest() } returns mockRequestModel


        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        mockCompletionListener = mockk(relaxed = true)
        mockEventServiceInternal = mockk(relaxed = true)
        mockNotificationCacheableEventHandler = mockk(relaxed = true)
        mockSilentMessageCacheableEventHandler = mockk(relaxed = true)
        pushInternal = DefaultPushInternal(
            mockRequestManager,
            concurrentHandlerHolder,
            mockRequestModelFactory,
            mockEventServiceInternal,
            mockPushTokenStorage,
            mockLocalPushTokenStorage,
            mockNotificationCacheableEventHandler,
            mockSilentMessageCacheableEventHandler,
            mockNotificationInformationListenerProvider,
            mockSilentNotificationInformationListenerProvider,
            true
        )
    }

    @Test
    fun testPushToken_shouldReturn_valueFromPushStorage_ifPushStorageIsNotEmpty() {
        every { mockPushTokenStorage.get() } returns PUSH_TOKEN
        every { mockLocalPushTokenStorage.get() } returns LOCAL_PUSH_TOKEN

        pushInternal.pushToken shouldBe PUSH_TOKEN
    }

    @Test
    fun testPushToken_shouldReturn_valueFromLocalStorage_ifPushStorage_isEmpty_andIsAutomaticPushSendingEnabled_isTrue() {
        every { mockPushTokenStorage.get() } returns null
        every { mockLocalPushTokenStorage.get() } returns LOCAL_PUSH_TOKEN

        pushInternal.pushToken shouldBe LOCAL_PUSH_TOKEN
    }

    @Test
    fun testPushToken_shouldReturn_valueFromLocalStorage_ifPushStorage_isEmpty_andIsAutomaticPushSendingEnabled_isFalse() {
        val testPushInternal = DefaultPushInternal(
            mockRequestManager,
            concurrentHandlerHolder,
            mockRequestModelFactory,
            mockEventServiceInternal,
            mockPushTokenStorage,
            mockLocalPushTokenStorage,
            mockNotificationCacheableEventHandler,
            mockSilentMessageCacheableEventHandler,
            mockNotificationInformationListenerProvider,
            mockSilentNotificationInformationListenerProvider,
            false
        )
        every { mockPushTokenStorage.get() } returns null
        every { mockLocalPushTokenStorage.get() } returns LOCAL_PUSH_TOKEN

        testPushInternal.pushToken shouldBe null
    }

    @Test
    fun testPushToken_shouldReturn_null_ifBothStoragesAreEmpty() {
        every { mockPushTokenStorage.get() } returns null
        every { mockLocalPushTokenStorage.get() } returns null

        pushInternal.pushToken shouldBe null
    }

    @Test
    fun testSetPushToken_shouldStoreToken_toLocalStorage() {
        pushInternal.setPushToken(PUSH_TOKEN, null)

        verify { mockLocalPushTokenStorage.set(PUSH_TOKEN) }
    }

    @Test
    fun testSetPushToken_whenRequest_success() {
        every { mockRequestManager.submit(mockRequestModel, any()) } answers {
            (it.invocation.args[1] as CompletionListener).onCompleted(null)
        }
        pushInternal.setPushToken(PUSH_TOKEN) {
            mockCompletionListener.onCompleted(null)
        }

        verify { mockRequestManager.submit(mockRequestModel, any()) }
        verify { mockPushTokenStorage.set(PUSH_TOKEN) }
        verify { mockCompletionListener.onCompleted(any()) }
    }

    @Test
    fun testSetPushToken_whenRequest_failure_shouldNotStorePushToken() {
        val mockError: Throwable = mockk(relaxed = true)
        every { mockRequestManager.submit(mockRequestModel, any()) } answers {
            (it.invocation.args[1] as CompletionListener).onCompleted(mockError)
        }

        pushInternal.setPushToken(PUSH_TOKEN) {
            mockCompletionListener.onCompleted(mockError)
        }

        verify { mockRequestManager.submit(mockRequestModel, any()) }
        verify { mockCompletionListener.onCompleted(mockError) }
        verify { mockPushTokenStorage.get() }
        confirmVerified(mockPushTokenStorage)
    }

    @Test
    fun testSetPushToken_doNotSendWhenPushTokenIsTheSame() {
        every { mockPushTokenStorage.get() } returns PUSH_TOKEN

        pushInternal.setPushToken(PUSH_TOKEN, mockCompletionListener)

        confirmVerified(mockRequestModelFactory)
        confirmVerified(mockRequestManager)
    }

    @Test
    fun testSetPushToken_shouldCallCompletionListener_onMainThread() {
        val threadSpy: ThreadSpy<CompletionListener> = ThreadSpy()
        every { mockPushTokenStorage.get() } returns PUSH_TOKEN
        every { mockCompletionListener.onCompleted(any()) } answers { threadSpy.call() }

        pushInternal.setPushToken(PUSH_TOKEN, mockCompletionListener)

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testGetPushToken_shouldGetPushTokenFromStorage() {
        every { mockPushTokenStorage.get() } returns PUSH_TOKEN

        val result = pushInternal.pushToken

        verify { mockPushTokenStorage.get() }
        result shouldBe PUSH_TOKEN
    }

    @Test
    fun testClearPushToken() {
        pushInternal.clearPushToken(mockCompletionListener)

        verify { mockPushTokenStorage.remove() }
        verify { mockRequestManager.submit(mockRequestModel, mockCompletionListener) }
    }

    @Test
    fun testRemovePushToken_completionListener_canBeNull() {
        pushInternal.clearPushToken(null)

        verify { mockRequestManager.submit(mockRequestModel, null) }
    }

    @Test
    fun testTrackMessageOpen() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        every {
            mockRequestModelFactory.createInternalCustomEventRequest(
                MESSAGE_OPEN_EVENT_NAME,
                attributes
            )
        } returns mockRequestModel

        pushInternal.trackMessageOpen(SID, mockCompletionListener)

        verify {
            mockEventServiceInternal.trackInternalCustomEventAsync(
                MESSAGE_OPEN_EVENT_NAME,
                attributes,
                mockCompletionListener
            )
        }
    }

    @Test
    fun testTrackMessageOpen_completionListener_canBeNull() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        every {
            mockRequestModelFactory.createInternalCustomEventRequest(
                MESSAGE_OPEN_EVENT_NAME,
                attributes
            )
        } returns mockRequestModel

        pushInternal.trackMessageOpen(SID, null)

        verify {
            mockEventServiceInternal.trackInternalCustomEventAsync(
                MESSAGE_OPEN_EVENT_NAME,
                attributes,
                null
            )
        }
    }

    @Test
    fun testTrackMessageOpen_completionListener_canBeNull_sidIsNull() {
        pushInternal.trackMessageOpen(null, null)
    }

    @Test
    fun testTrackMessageOpen_shouldCallCompletionListenerWithError_whenSidNotFound() {
        val completionListener: CompletionListener = mockk(relaxed = true)
        val countDownLatch = CountDownLatch(1)
        val fakeCompletionListener = FakeCompletionListener(countDownLatch, completionListener)
        val exceptionSlot: CapturingSlot<IllegalArgumentException> = slot()

        pushInternal.trackMessageOpen(null, fakeCompletionListener)

        countDownLatch.await()


        verify { completionListener.onCompleted(capture(exceptionSlot)) }
        exceptionSlot.captured.message shouldBe "No messageId found!"
        exceptionSlot.captured.shouldBeTypeOf<IllegalArgumentException>()

    }

    @Test
    fun testTrackMessageOpen_withEmptyIntent_shouldCallCompletionListener_onMainThread() {
        val completionListener: CompletionListener = mockk(relaxed = true)
        val threadSpy: ThreadSpy<CompletionListener> = ThreadSpy()
        every { completionListener.onCompleted(any()) } answers { threadSpy.call() }

        pushInternal.trackMessageOpen(null, completionListener)

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testSetNotificationEventHandler_shouldSetInProvider() {
        val mockEventHandler: EventHandler = mockk(relaxed = true)
        pushInternal.setNotificationEventHandler(mockEventHandler)

        verify { mockNotificationCacheableEventHandler.setEventHandler(mockEventHandler) }
    }

    @Test
    fun testSetSilentMessageEventHandler_shouldSetInProvider() {
        val mockEventHandler: EventHandler = mockk(relaxed = true)
        pushInternal.setSilentMessageEventHandler(mockEventHandler)

        verify { mockSilentMessageCacheableEventHandler.setEventHandler(mockEventHandler) }
    }

    @Test
    fun testSetNotificationInformationListener() {
        val mockNotificationInformationListener: NotificationInformationListener =
            mockk(relaxed = true)
        pushInternal.setNotificationInformationListener(mockNotificationInformationListener)


        verify {
            mockNotificationInformationListenerProvider.notificationInformationListener =
                mockNotificationInformationListener
        }

    }

    @Test
    fun testSetSilentNotificationInformationListener() {
        val mockSilentNotificationInformationListener: NotificationInformationListener =
            mockk(relaxed = true)
        pushInternal.setSilentNotificationInformationListener(
            mockSilentNotificationInformationListener
        )

        verify {
            mockSilentNotificationInformationListenerProvider.silentNotificationInformationListener =
                mockSilentNotificationInformationListener
        }
    }
}