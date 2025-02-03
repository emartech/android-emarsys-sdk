package com.emarsys.mobileengage.event


import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.spy

class DefaultEventServiceInternalTest  {

    companion object {
        const val REQUEST_ID = "request_id"

        const val CONTACT_FIELD_ID = 3
        const val EVENT_NAME = "customEventName"

        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var eventServiceInternal: EventServiceInternal


    @Before
    fun setUp() {
        mockRequestModel = mock(RequestModel::
class.java)
        mockRequestManager = mock(RequestManager::
class.java)
        mockCompletionListener = mock(CompletionListener::
class.java)

        mockRequestModelFactory = mock(MobileEngageRequestModelFactory::
class.java).apply {
            whenever(createCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(
                mockRequestModel
            )
            whenever(createInternalCustomEventRequest(EVENT_NAME, EVENT_ATTRIBUTES)).thenReturn(
                mockRequestModel
            )
        }

        eventServiceInternal = DefaultEventServiceInternal(mockRequestModelFactory, mockRequestManager)
    }

    @Test
    fun testTrackCustomEventAsync_shouldDelegateToTrackCustomEvent() {
        val eventServiceInternal = spy(this.eventServiceInternal)
        val completionListener = CompletionListener { }

        eventServiceInternal.trackCustomEventAsync(EVENT_NAME, EVENT_ATTRIBUTES, completionListener)

        verify(eventServiceInternal).trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, completionListener)
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

    @Test
    fun testTrackInternalCustomEvent() {
        eventServiceInternal.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEventAsync_shouldDelegateToTrackCustomEvent() {
        val eventServiceInternal = spy(this.eventServiceInternal)
        val completionListener = CompletionListener { }

        eventServiceInternal.trackInternalCustomEventAsync(EVENT_NAME, EVENT_ATTRIBUTES, completionListener)

        verify(eventServiceInternal).trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, completionListener)
    }

    @Test
    fun testTrackInternalCustomEvent_completionListener_canBeNull() {
        eventServiceInternal.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }
}