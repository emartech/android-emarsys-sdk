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
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.Trigger
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.json.JSONObject
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
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)

        geofenceInternal.enable(null)

        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted_withCompletionListener() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        var completionListenerHasBeenCalled = false

        geofenceInternal.enable(CompletionListener {
            it shouldBe null
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenFineLocationPermissionDenied() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)

        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
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
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenBackgroundLocationPermissionDenied() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)

        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable(CompletionListener {
            it is MissingPermissionException
            it?.message shouldBe "Couldn't acquire permission for ACCESS_BACKGROUND_LOCATION"
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenPermissionsDenied() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)

        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable(CompletionListener {
            it is MissingPermissionException
            it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION"
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_whenLocationManagerIsNull() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, null, mockGeofenceFilter)

        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable(CompletionListener {
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_registersGeofencesWithAdditionalRefreshArea() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter)
        val geofenceResponse = GeofenceResponse(listOf(), 0.3)

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        val spyGeofenceInternal: GeofenceInternal = spy(geofenceInternal)
        spyGeofenceInternal.fetchGeofences()
        val refreshTrigger = Trigger(id = "refreshAreaTriggerId", type = TriggerType.EXIT, loiteringDelay = 0, action = JSONObject())
        val trigger = Trigger(id = "refreshAreaTriggerId", type = TriggerType.ENTER, action = JSONObject())
        val refreshArea = Geofence("refreshArea", 47.493160, 19.058355, 49.079566955566406, null, listOf(refreshTrigger))
        val nearestGeofences = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                Geofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )
        val geofencesToRegister = nearestGeofences + refreshArea

        whenever(mockGeofenceFilter.findNearestGeofences(any(), any())).thenReturn(nearestGeofences)
        val currentLocation = (Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        })

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(currentLocation)

        spyGeofenceInternal.enable(null)
        argumentCaptor<List<Geofence>>().apply {
            verify(spyGeofenceInternal).registerGeofences(capture())
            allValues[0].size shouldBe 4
            allValues[0][3].toString() shouldBe refreshArea.toString()
        }
    }
}