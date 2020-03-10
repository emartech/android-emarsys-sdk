package com.emarsys.mobileengage.geofence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.fake.FakeRequestManager
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.Trigger
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import com.emarsys.mobileengage.geofence.model.Geofence as MEGeofence

class DefaultGeofenceInternalTest {

    private companion object {
        val refreshTrigger = Trigger(id = "refreshAreaTriggerId", type = TriggerType.EXIT, loiteringDelay = 0, action = JSONObject())
        val trigger = Trigger(id = "refreshAreaTriggerId", type = TriggerType.ENTER, action = JSONObject())
        val refreshArea = MEGeofence("refreshArea", 47.493160, 19.058355, 49.079566955566406, null, listOf(refreshTrigger))
        val nearestGeofencesWithoutRefreshArea: List<MEGeofence> = listOf(
                MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                MEGeofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var mockFetchGeofenceRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var fakeRequestManager: RequestManager
    private lateinit var mockLocationManager: LocationManager
    private lateinit var mockGeofenceResponseMapper: GeofenceResponseMapper
    private lateinit var geofenceInternal: GeofenceInternal
    private lateinit var mockPermissionChecker: PermissionChecker
    private lateinit var mockGeofenceFilter: GeofenceFilter
    private lateinit var mockLocation: Location
    private lateinit var mockGeofencingClient: GeofencingClient
    private lateinit var context: Context

    @Before
    fun setUp() {

        mockFetchGeofenceRequestModel = mock()
        mockResponseModel = mock()
        mockRequestModelFactory = mock {
            on { createFetchGeofenceRequest() } doReturn mockFetchGeofenceRequestModel
        }
        fakeRequestManager = spy(FakeRequestManager(FakeRequestManager.ResponseType.SUCCESS, mockResponseModel))
        mockGeofenceResponseMapper = mock()
        mockPermissionChecker = mock()
        mockLocationManager = mock()
        mockGeofenceFilter = mock()
        mockLocation = mock()
        mockGeofencingClient = mock()
        context = InstrumentationRegistry.getTargetContext()

        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockLocationManager, mockGeofenceFilter, mockGeofencingClient, context)
    }

    @Test
    fun testFetchGeofences_shouldSendRequest_viaRequestManager_submitNow() {
        geofenceInternal.fetchGeofences()

        verify(fakeRequestManager).submitNow(any(), any())
    }

    @Test
    fun testFetchGeofences_callsMapOnResponseMapper_onSuccess() {
        geofenceInternal.fetchGeofences()

        verify(mockGeofenceResponseMapper).map(mockResponseModel)
    }

    @Test
    fun testEnable_checksForLocationPermissions_throughPermissionChecker() {
        geofenceInternal.enable(null)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)

        geofenceInternal.enable(null)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenFineLocationPermissionDenied() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenBackgroundLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable(CompletionListener {
            it is MissingPermissionException
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it?.message shouldBe "Couldn't acquire permission for ACCESS_BACKGROUND_LOCATION"
            }
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            verifyZeroInteractions(mockLocationManager)
            verifyZeroInteractions(mockGeofenceFilter)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenPermissionsDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation)
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable(CompletionListener {
            it is MissingPermissionException
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION"
            } else {
                it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION"
            }
            completionListenerHasBeenCalled = true
        })

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_whenLocationManagerIsNull() {
        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, null, mockGeofenceFilter, mockGeofencingClient, context)
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verifyZeroInteractions(mockLocationManager)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_registersGeofencesWithAdditionalRefreshArea() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.3)

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        val spyGeofenceInternal: GeofenceInternal = spy(geofenceInternal)
        spyGeofenceInternal.fetchGeofences()

        whenever(mockGeofenceFilter.findNearestGeofences(any(), any())).thenReturn(nearestGeofencesWithoutRefreshArea)
        val currentLocation = (Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        })

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(currentLocation)

        spyGeofenceInternal.enable(null)
        argumentCaptor<List<MEGeofence>>().apply {
            verify(spyGeofenceInternal).registerGeofences(capture())
            allValues[0].size shouldBe 4
            allValues[0][3].toString() shouldBe refreshArea.toString()
        }
    }

    @Test
    fun testRegisterGeofences_geofencingClientAddsNearestGeofences() {
        val geofencesToTest = nearestGeofencesWithoutRefreshArea.map { createGeofence(it) } + createGeofence(Companion.refreshArea)
        val geofencingRequest = GeofencingRequest.Builder().addGeofences(geofencesToTest).build()

        geofenceInternal.registerGeofences(nearestGeofencesWithoutRefreshArea + refreshArea)

        argumentCaptor<GeofencingRequest>().apply {
            verify(mockGeofencingClient).addGeofences(capture(), any())

            allValues[0].initialTrigger shouldBe geofencingRequest.initialTrigger
            allValues[0].geofences.forEachIndexed { index, geofence ->
                geofence.requestId shouldBe geofencingRequest.geofences[index].requestId
            }
        }
    }

    private fun createGeofence(geofence: MEGeofence): Geofence? {
        return Geofence.Builder()
                .setRequestId(geofence.id)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Long.MAX_VALUE)
                .setCircularRegion(geofence.lon, geofence.lat, geofence.radius.toFloat())
                .build()
    }
}