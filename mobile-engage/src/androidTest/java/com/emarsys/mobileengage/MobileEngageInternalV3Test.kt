package com.emarsys.mobileengage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.fake.FakeCompletionListener
import com.emarsys.mobileengage.request.RequestModelFactory
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
        const val MESSAGE_OPEN_EVENT_NAME = "push:click"
        const val SID = "+43c_lODSmXqCvdOz"

        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    private lateinit var mobileEngageInternal: MobileEngageInternalV3

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestContext: RequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockRequestModelFactory: RequestModelFactory
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
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
            whenever(refreshTokenStorage).thenReturn(mockRefreshTokenStorage)
            whenever(contactTokenStorage).thenReturn(mockContactTokenStorage)
            whenever(clientStateStorage).thenReturn(mockClientStateStorage)
        }

        mockRequestModel = mock(RequestModel::class.java)

        mockRequestModelFactory = mock(RequestModelFactory::class.java).apply {
            whenever(createSetContactRequest(CONTACT_FIELD_VALUE)).thenReturn(mockRequestModel)
            whenever(createSetPushTokenRequest(PUSH_TOKEN)).thenReturn(mockRequestModel)
            whenever(createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createTrackDeviceInfoRequest()).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
        }

        uiHandler = Handler(Looper.getMainLooper())

        mockCompletionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = MobileEngageInternalV3(mockRequestManager, uiHandler, mockRequestModelFactory, mockRequestContext, mockEventServiceInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        MobileEngageInternalV3(null, uiHandler, mockRequestModelFactory, mockRequestContext, mockEventServiceInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uiHandler_mustNotBeNull() {
        MobileEngageInternalV3(mockRequestManager, null, mockRequestModelFactory, mockRequestContext, mockEventServiceInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        MobileEngageInternalV3(mockRequestManager, uiHandler, null, mockRequestContext, mockEventServiceInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageInternalV3(mockRequestManager, uiHandler, mockRequestModelFactory, null, mockEventServiceInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_eventServiceInternal_mustNotBeNull() {
        MobileEngageInternalV3(mockRequestManager, uiHandler, mockRequestModelFactory, mockRequestContext, null)
    }

    @Test
    fun testSetPushToken() {
        mobileEngageInternal.setPushToken(PUSH_TOKEN, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testSetPushToken_completionListener_canBeNull() {
        mobileEngageInternal.setPushToken(PUSH_TOKEN, null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test
    fun testSetPushToken_whenPushTokenIsNull_callShouldBeIgnored() {
        mobileEngageInternal.setPushToken(null, mockCompletionListener)

        verifyZeroInteractions(mockRequestManager)
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

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCustomEvent_eventName_mustNotBeNull() {
        mobileEngageInternal.trackCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackCustomEvent() {
        mobileEngageInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockEventServiceInternal).trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

    }

    @Test
    fun testTrackCustomEvent_completionListener_canBeNull() {
        mobileEngageInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)

        verify(mockEventServiceInternal).trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackInternalCustomEvent_eventName_mustNotBeNull() {
        mobileEngageInternal.trackInternalCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent() {
        mobileEngageInternal.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockEventServiceInternal).trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent_completionListener_canBeNull() {
        mobileEngageInternal.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)

        verify(mockEventServiceInternal).trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)
    }

    @Test
    fun testGetMessageId_shouldReturnNull_withEmptyIntent() {
        val result = mobileEngageInternal.getMessageId(Intent())
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

        val result = mobileEngageInternal.getMessageId(intent)
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

        val result = mobileEngageInternal.getMessageId(intent)
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

        val result = mobileEngageInternal.getMessageId(intent)

        result shouldBe null
    }

    @Test
    fun testGetMessageId_shouldReturnTheCorrectSIDValue() {
        val intent = createTestIntent()
        val result = mobileEngageInternal.getMessageId(intent)

        result shouldBe SID
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackMessageOpen_intent_mustNotBeNull() {
        mobileEngageInternal.trackMessageOpen(null, mockCompletionListener)
    }

    @Test
    fun testTrackMessageOpen() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        whenever(mockRequestModelFactory.createInternalCustomEventRequest(MESSAGE_OPEN_EVENT_NAME, attributes)).thenReturn(mockRequestModel)

        mobileEngageInternal.trackMessageOpen(createTestIntent(), mockCompletionListener)

        verify(mockEventServiceInternal).trackInternalCustomEvent(MESSAGE_OPEN_EVENT_NAME, attributes, mockCompletionListener)
    }

    @Test
    fun testTrackMessageOpen_completionListener_canBeNull() {
        val attributes = mapOf("sid" to SID, "origin" to "main")
        whenever(mockRequestModelFactory.createInternalCustomEventRequest(MESSAGE_OPEN_EVENT_NAME, attributes)).thenReturn(mockRequestModel)

        mobileEngageInternal.trackMessageOpen(createTestIntent(), null)

        verify(mockEventServiceInternal).trackInternalCustomEvent(MESSAGE_OPEN_EVENT_NAME, attributes, null)
    }

    @Test
    fun testTrackMessageOpen_shouldCallCompletionListenerWithError_whenMessageIdNotFound() {
        val completionListener = mock(CompletionListener::class.java)
        val countDownLatch = CountDownLatch(1)
        val fakeCompletionListener = FakeCompletionListener(countDownLatch, completionListener)

        mobileEngageInternal.trackMessageOpen(createBadTestIntent(), fakeCompletionListener)

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

        mobileEngageInternal.trackMessageOpen(createBadTestIntent(), completionListener)

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testTrackDeviceInfo() {
        mobileEngageInternal.trackDeviceInfo()

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
