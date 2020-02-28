package com.emarsys.geofence

import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.FakeRequestManager
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class DefaultGeofenceInternalTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var mockFetchGeofenceRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModelFactory: EmarsysRequestModelFactory
    private lateinit var fakeRequestManager: RequestManager
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockGeofenceResponseMapper: GeofenceResponseMapper
    private lateinit var geofenceInternal: GeofenceInternal

    @Before
    fun setUp() {

        mockFetchGeofenceRequestModel = mock()
        mockResponseModel = mock()
        mockRequestModelFactory = mock {
            on { createFetchGeofenceRequest() } doReturn mockFetchGeofenceRequestModel
        }
        fakeRequestManager = FakeRequestManager(FakeRequestManager.ResponseType.SUCCESS, mockResponseModel)
        mockRequestManager = mock()
        mockGeofenceResponseMapper = mock()
    }
    
    @Test
    fun testFetchGeofences_shouldSendRequest_viaRequestManager_submitNow() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, mockRequestManager, mockGeofenceResponseMapper)

        geofenceInternal.fetchGeofences()

        verify(mockRequestManager).submitNow(any(), any())
    }

    @Test
    fun testFetchGeofences_callsMapOnResponseMapper_onSuccess() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper)

        geofenceInternal.fetchGeofences()

        verify(mockGeofenceResponseMapper).map(mockResponseModel)
    }
}