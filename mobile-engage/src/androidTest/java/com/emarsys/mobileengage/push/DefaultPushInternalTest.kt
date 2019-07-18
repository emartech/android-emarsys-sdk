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
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.RequestContext
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
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
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
    private lateinit var mockRequestContext: RequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
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

        mockContactFieldValueStorage = mock(Storage::class.java) as Storage<String>
        mockRefreshTokenStorage = mock(Storage::class.java) as Storage<String>
        mockContactTokenStorage = mock(Storage::class.java) as Storage<String>
        mockClientStateStorage = mock(Storage::class.java) as Storage<String>

        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockRequestManager = mock(RequestManager::class.java)
        mockRequestContext = mock(RequestContext::class.java)

        mockRequestModel = mock(RequestModel::class.java)

        mockRequestModelFactory = mock(MobileEngageRequestModelFactory::class.java).apply {
            whenever(createSetPushTokenRequest(PUSH_TOKEN)).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createRemovePushTokenRequest()).thenReturn(mockRequestModel)
        }

        uiHandler = Handler(Looper.getMainLooper())

        mockCompletionListener = mock(CompletionListener::class.java)
        mockEventServiceInternal = mock(EventServiceInternal::class.java)
        pushInternal = DefaultPushInternal(mockRequestManager, uiHandler, mockRequestModelFactory, mockEventServiceInternal)
    }

    @Test
    fun testSetPushToken() {
        pushInternal.setPushToken(PUSH_TOKEN, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testSetPushToken_completionListener_canBeNull() {
        pushInternal.setPushToken(PUSH_TOKEN, null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test
    fun testSetPushToken_whenPushTokenIsNull_callShouldBeIgnored() {
        pushInternal.setPushToken(null, mockCompletionListener)

        verifyZeroInteractions(mockRequestManager)
    }

    @Test
    fun testRemovePushToken() {
        pushInternal.clearPushToken(mockCompletionListener)

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

    @Test(expected = IllegalArgumentException::class)
    fun testTrackMessageOpen_intent_mustNotBeNull() {
        pushInternal.trackMessageOpen(null, mockCompletionListener)
    }

    @Test
    fun testTrackMessageOpen() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        whenever(mockRequestModelFactory.createInternalCustomEventRequest(MESSAGE_OPEN_EVENT_NAME, attributes)).thenReturn(mockRequestModel)

        pushInternal.trackMessageOpen(createTestIntent(), mockCompletionListener)

        verify(mockEventServiceInternal).trackInternalCustomEvent(MESSAGE_OPEN_EVENT_NAME, attributes, mockCompletionListener)
    }

    @Test
    fun testTrackMessageOpen_completionListener_canBeNull() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        whenever(mockRequestModelFactory.createInternalCustomEventRequest(MESSAGE_OPEN_EVENT_NAME, attributes)).thenReturn(mockRequestModel)

        pushInternal.trackMessageOpen(createTestIntent(), null)

        verify(mockEventServiceInternal).trackInternalCustomEvent(MESSAGE_OPEN_EVENT_NAME, attributes, null)
    }

    @Test
    fun testTrackMessageOpen_shouldCallCompletionListenerWithError_whenMessageIdNotFound() {
        val completionListener = mock(CompletionListener::class.java)
        val countDownLatch = CountDownLatch(1)
        val fakeCompletionListener = FakeCompletionListener(countDownLatch, completionListener)

        pushInternal.trackMessageOpen(createBadTestIntent(), fakeCompletionListener)

        countDownLatch.await()

        val captor = ArgumentCaptor.forClass(IllegalArgumentException::class.java)

        verify(completionListener).onCompleted(captor.capture())

        captor.value.message shouldBe "No messageId found!"
        captor.value.shouldBeTypeOf<IllegalArgumentException>()
    }

    @Test
    fun testTrackMessageOpen_withEmptyIntent_shouldCallCompletionListener_onMainThread() {
        val completionListener = mock(CompletionListener::class.java)
        val threadSpy: ThreadSpy<CompletionListener> = ThreadSpy()
        doAnswer(threadSpy).`when`(completionListener).onCompleted(any(Throwable::class.java))

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
}