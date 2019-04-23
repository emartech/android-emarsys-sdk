package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.request.RequestModelFactory
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class EventServiceInternalV3Test {
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockRequestModelFactory: RequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var eventServiceInternal: EventServiceInternal

    @Before
    fun setUp() {
        mockRequestModel = mock(RequestModel::class.java)
        mockRequestManager = mock(RequestManager::class.java)
        mockCompletionListener = mock(CompletionListener::class.java)

        mockRequestModelFactory = mock(RequestModelFactory::class.java).apply {
            whenever(createCustomEventRequest(MobileEngageInternalV3Test.EVENT_NAME, MobileEngageInternalV3Test.EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
            whenever(createInternalCustomEventRequest(MobileEngageInternalV3Test.EVENT_NAME, MobileEngageInternalV3Test.EVENT_ATTRIBUTES)).thenReturn(mockRequestModel)
        }

        eventServiceInternal = EventServiceInternalV3(mockRequestModelFactory, mockRequestManager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        EventServiceInternalV3(null, mockRequestManager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        EventServiceInternalV3(mockRequestModelFactory, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCustomEvent_eventName_mustNotBeNull() {
        eventServiceInternal.trackCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackCustomEvent() {
        eventServiceInternal.trackCustomEvent(MobileEngageInternalV3Test.EVENT_NAME, MobileEngageInternalV3Test.EVENT_ATTRIBUTES, mockCompletionListener)

        Mockito.verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testTrackCustomEvent_completionListener_canBeNull() {
        eventServiceInternal.trackCustomEvent(MobileEngageInternalV3Test.EVENT_NAME, MobileEngageInternalV3Test.EVENT_ATTRIBUTES, null)

        Mockito.verify(mockRequestManager).submit(mockRequestModel, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackInternalCustomEvent_eventName_mustNotBeNull() {
        eventServiceInternal.trackInternalCustomEvent(null, emptyMap(), mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent() {
        eventServiceInternal.trackInternalCustomEvent(MobileEngageInternalV3Test.EVENT_NAME, MobileEngageInternalV3Test.EVENT_ATTRIBUTES, mockCompletionListener)

        Mockito.verify(mockRequestManager).submit(mockRequestModel, mockCompletionListener)
    }

    @Test
    fun testTrackInternalCustomEvent_completionListener_canBeNull() {
        eventServiceInternal.trackInternalCustomEvent(MobileEngageInternalV3Test.EVENT_NAME, MobileEngageInternalV3Test.EVENT_ATTRIBUTES, null)

        Mockito.verify(mockRequestManager).submit(mockRequestModel, null)
    }
}