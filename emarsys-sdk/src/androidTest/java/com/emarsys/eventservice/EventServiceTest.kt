package com.emarsys.eventservice

import com.emarsys.core.api.result.CompletionListener

import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EventServiceTest {
    companion object {
        private const val EVENT_NAME = "testEventName"
        private val EVENT_ATTRIBUTES = mapOf<String, String?>(
                "key1" to "value1",
                "key2" to "value2"
        )
    }

    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var eventServiceApi: EventServiceApi

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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