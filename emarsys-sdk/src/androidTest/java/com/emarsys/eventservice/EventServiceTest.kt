package com.emarsys.eventservice


import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.IntegrationTestUtils
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EventServiceTest : AnnotationSpec() {
    companion object {
        private const val EVENT_NAME = "testEventName"
        private val EVENT_ATTRIBUTES = mapOf(
            "key1" to "value1",
            "key2" to "value2"
        )
    }

    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var eventServiceApi: EventServiceApi


    @Before
    fun setUp() {
        mockCompletionListener = mock()
        mockEventServiceInternal = mock()
        eventServiceApi = EventService()

        setupEmarsysComponent(FakeDependencyContainer(eventServiceInternal = mockEventServiceInternal))
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testEventServiceApi_trackCustomEvent_delegatesToDefaultInstance() {
       eventServiceApi.trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
        verify(mockEventServiceInternal).trackCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }

    @Test
    fun testEventServiceApi_trackCustomEventAsync_delegatesToDefaultInstance() {
       eventServiceApi.trackCustomEventAsync(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
        verify(mockEventServiceInternal).trackCustomEventAsync(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }

    @Test
    fun testEventServiceApi_trackInternalCustomEvent_delegatesToDefaultInstance() {
       eventServiceApi.trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
        verify(mockEventServiceInternal).trackInternalCustomEvent(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }

    @Test
    fun testEventServiceApi_trackInternalCustomEventAsync_delegatesToDefaultInstance() {
       eventServiceApi.trackInternalCustomEventAsync(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
        verify(mockEventServiceInternal).trackInternalCustomEventAsync(EVENT_NAME, EVENT_ATTRIBUTES, mockCompletionListener)
    }
}