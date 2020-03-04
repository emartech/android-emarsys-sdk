package com.emarsys.mobileengage.geofence

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.fake.FakeRequestManager
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
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
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var fakeRequestManager: RequestManager
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockLocationManager: LocationManager
    private lateinit var mockGeofenceResponseMapper: GeofenceResponseMapper
    private lateinit var geofenceInternal: GeofenceInternal
    private lateinit var mockPermissionChecker: PermissionChecker
    private lateinit var mockGeofenceFilter: GeofenceFilter
    private lateinit var mockLocation: Location

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
        mockPermissionChecker = mock()
        mockLocationManager = mock()
        mockGeofenceFilter = mock()
        mockLocation = mock()

        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, mockRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)
    }

    @Test
    fun testFetchGeofences_shouldSendRequest_viaRequestManager_submitNow() {
        geofenceInternal.fetchGeofences()

        verify(mockRequestManager).submitNow(any(), any())
    }

    @Test
    fun testFetchGeofences_callsMapOnResponseMapper_onSuccess() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)

        geofenceInternal.fetchGeofences()

        verify(mockGeofenceResponseMapper).map(mockResponseModel)
    }

    @Test
    fun testEnable_checksForLocationPermissions_throughPermissionChecker() {
        geofenceInternal.enable(null)

        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)

        geofenceInternal.enable(null)

        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted_withCompletionListener() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        var completionListenerHasBeenCalled = false

        geofenceInternal.enable(CompletionListener {
            it shouldBe null
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun testEnable_callsFindNearestGeofencesOnGeofenceFilter_whenPermissionGranted_andFetchingWasSuccessful() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)

        val geofenceResponse = GeofenceResponse(listOf(), 0.0)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)
        var completionListenerHasBeenCalled = false

        geofenceInternal.fetchGeofences()

        geofenceInternal.enable(CompletionListener {
            it shouldBe null
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
        verify(mockGeofenceFilter).findNearestGeofences(eq(mockLocation), any())
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenPermissionDenied() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)

        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable(CompletionListener {
            it is MissingPermissionException
            it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION"
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }
}