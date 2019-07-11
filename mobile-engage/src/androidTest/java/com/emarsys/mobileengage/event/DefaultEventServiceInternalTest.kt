package com.emarsys.mobileengage.event

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.request.RequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DefaultEventServiceInternalTest {

    companion object {
        const val REQUEST_ID = "request_id"

        const val CONTACT_FIELD_ID = 3
        const val EVENT_NAME = "customEventName"

        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockRequestModelFactory: RequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var eventServiceInternal: EventServiceInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRequestModel = mock(RequestModel::class.java)
        mockRequestManager = mock(RequestManager::class.java)
        mockCompletionListener = mock(CompletionListener::class.java)

        mockRequestModelFactory = mock(RequestModelFactory::class.java).apply {
            whenever(createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
        }

        eventServiceInternal = DefaultEventServiceInternal(mockRequestManager, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        DefaultEventServiceInternal(mockRequestManager, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        DefaultEventServiceInternal(null, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCustomEvent_eventName_mustNotBeNull() {
        eventServiceInternal.trackCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackCustomEvent() {
        eventServiceInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testTrackCustomEvent_completionListener_canBeNull() {
        eventServiceInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackInternalCustomEvent_eventName_mustNotBeNull() {
        eventServiceInternal.trackInternalCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent() {
        eventServiceInternal.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent_completionListener_canBeNull() {
        eventServiceInternal.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }
}