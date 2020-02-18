package com.emarsys.mobileengage.iam

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class InAppInternalTest {

    private companion object {
        const val EVENT_NAME = "customEventName"
        val EVENT_ATTRIBUTES = emptyMap<String, String>()
    }

    private lateinit var inAppInternal: InAppInternal
    private lateinit var mockInAppEventHandlerInternal: InAppEventHandlerInternal
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockEventServiceInternal: EventServiceInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {

        mockRequestModel = mock(RequestModel::class.java)
        mockCompletionListener = mock(CompletionListener::class.java)
        mockEventServiceInternal = mock(EventServiceInternal::class.java)
        mockInAppEventHandlerInternal = mock(InAppEventHandlerInternal::class.java)

        inAppInternal = DefaultInAppInternal(mockInAppEventHandlerInternal, mockEventServiceInternal)
    }


    @Test
    fun testIsPaused() {
        inAppInternal.isPaused

        verify(mockInAppEventHandlerInternal).isPaused
    }

    @Test
    fun testPause() {
        inAppInternal.pause()

        verify(mockInAppEventHandlerInternal).pause()
    }

    @Test
    fun testResume() {
        inAppInternal.resume()

        verify(mockInAppEventHandlerInternal).resume()
    }

    @Test
    fun testGetEventHandler() {
        inAppInternal.eventHandler

        verify(mockInAppEventHandlerInternal).eventHandler
    }

    @Test
    fun testSetEventHandler() {
        val mockEventHandler = mock(EventHandler::class.java)
        inAppInternal.eventHandler = mockEventHandler

        verify(mockInAppEventHandlerInternal).eventHandler = mockEventHandler
    }


    @Test(expected = IllegalArgumentException::class)
    fun testTrackCustomEvent_eventName_mustNotBeNull() {
        inAppInternal.trackCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackCustomEvent() {
        inAppInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockEventServiceInternal).trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackInternalCustomEvent_eventName_mustNotBeNull() {
        inAppInternal.trackInternalCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent() {
        inAppInternal.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)

        verify(mockEventServiceInternal).trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }

}