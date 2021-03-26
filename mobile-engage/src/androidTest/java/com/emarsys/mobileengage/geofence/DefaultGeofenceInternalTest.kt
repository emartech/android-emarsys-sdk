package com.emarsys.mobileengage.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.fake.FakeRequestManager
import com.emarsys.mobileengage.geofence.model.*
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.google.android.gms.location.FusedLocationProviderClient
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
import java.util.concurrent.CountDownLatch
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

    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var fakeRequestManager: RequestManager
    private lateinit var mockFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mockGeofenceResponseMapper: GeofenceResponseMapper
    private lateinit var geofenceInternal: GeofenceInternal
    private lateinit var mockPermissionChecker: PermissionChecker
    private lateinit var mockGeofenceFilter: GeofenceFilter
    private lateinit var mockLocation: Location
    private lateinit var mockGeofencingClient: GeofencingClient
    private lateinit var context: Context
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockContext: Context
    private lateinit var mockEventHandlerProvider: EventHandlerProvider
    private lateinit var geofenceInternalWithMockContext: GeofenceInternal
    private lateinit var mockEnabledStorage: Storage<Boolean>
    private lateinit var mockPendingIntentProvider: GeofencePendingIntentProvider
    private lateinit var mockHandler: CoreSdkHandler

    @Before
    fun setUp() {
        mockHandler = mock()
        context = InstrumentationRegistry.getTargetContext()
        mockResponseModel = mock()
        mockRequestModelFactory = mock {
            on { createFetchGeofenceRequest() } doReturn mock()
        }
        val intent = Intent("com.emarsys.sdk.GEOFENCE_ACTION")
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        mockPendingIntentProvider = mock {
            on { providePendingIntent() } doReturn pendingIntent
        }

        fakeRequestManager = spy(FakeRequestManager(FakeRequestManager.ResponseType.SUCCESS, mockResponseModel))
        mockGeofenceResponseMapper = mock()
        mockPermissionChecker = mock()
        mockFusedLocationProviderClient = mock()
        mockGeofenceFilter = mock()
        mockLocation = mock()
        mockGeofencingClient = mock()

        mockContext = mock()
        mockActionCommandFactory = mock()
        mockEventHandlerProvider = mock()
        mockEnabledStorage = mock {
            on { get() } doReturn true
        }

        geofenceInternal = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockFusedLocationProviderClient, mockGeofenceFilter, mockGeofencingClient, context, mockActionCommandFactory, mockEventHandlerProvider, mockEnabledStorage, mockPendingIntentProvider, mockHandler)

        geofenceInternalWithMockContext = DefaultGeofenceInternal(mockRequestModelFactory, fakeRequestManager, mockGeofenceResponseMapper, mockPermissionChecker, mockFusedLocationProviderClient, mockGeofenceFilter, mockGeofencingClient, mockContext, mockActionCommandFactory, mockEventHandlerProvider, mockEnabledStorage, mockPendingIntentProvider, mockHandler)
    }

    @Test
    fun testFetchGeofences_shouldSendRequest_viaRequestManager_submitNow() {
        geofenceInternal.fetchGeofences(null)

        verify(fakeRequestManager).submitNow(any(), any())
    }

    @Test
    fun testFetchGeofences_callsMapOnResponseMapper_onSuccess() {
        geofenceInternal.fetchGeofences(null)

        verify(mockGeofenceResponseMapper).map(mockResponseModel)
    }

    @Test
    fun testFetchGeofences_callsEnable_onSuccess() {
        val spyGeofenceInternal = spy(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        verify(spyGeofenceInternal).enable(null)
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
    fun testEnable_registersGeofenceBroadcastReceiver() {
        geofenceInternalWithMockContext.enable(null)
        geofenceInternalWithMockContext.enable(null)

        verify(mockContext, timeout(100).times(1)).registerReceiver(any<GeofenceBroadcastReceiver>(), any())
    }

    @Test
    fun testDisable_unregistersGeofenceBroadcastReceiver() {
        geofenceInternalWithMockContext.disable()

        verify(mockContext).unregisterReceiver(any<GeofenceBroadcastReceiver>())
        verify(mockFusedLocationProviderClient).removeLocationUpdates(mockPendingIntentProvider.providePendingIntent())
    }

    @Test
    fun testDisable() {
        geofenceInternalWithMockContext.enable(null)
        geofenceInternalWithMockContext.disable()
        geofenceInternalWithMockContext.enable(null)

        verify(mockContext, timeout(100).times(2)).registerReceiver(any<GeofenceBroadcastReceiver>(), any())
    }

    @Test
    fun testFetch_doNotFetch_whenFetchIsNotEnabled() {
        whenever(mockEnabledStorage.get()).thenReturn(false)

        geofenceInternal.fetchGeofences(null)

        verifyNoMoreInteractions(mockRequestModelFactory)
        verifyNoMoreInteractions(fakeRequestManager)
    }

    @Test
    fun testFetch_fetch_whenFetchIsEnabled() {
        whenever(mockEnabledStorage.get()).thenReturn(true)

        geofenceInternal.fetchGeofences(null)

        verify(mockRequestModelFactory).createFetchGeofenceRequest()
    }

    @Test
    fun testEnable_shouldSetEnabledStorage_andFetchIfWasDisabled() {
        val latch = CountDownLatch(1)
        var completionListenerHasBeenCalled = false
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockEnabledStorage.get()).thenReturn(false).thenReturn(true)
        geofenceInternal.enable {
            it shouldBe null
            completionListenerHasBeenCalled = true
            latch.countDown()
        }

        latch.await()
        completionListenerHasBeenCalled shouldBe true
        verify(mockEnabledStorage, timeout(100)).set(true)
        verify(mockRequestModelFactory).createFetchGeofenceRequest()
    }

    @Test
    fun testDisable_shouldClearEnabledStorage() {
        geofenceInternalWithMockContext.disable()

        verify(mockEnabledStorage).set(false)
    }

    @Test
    fun testIsEnabled_shouldReturnTrue_whenGeofenceIsEnabled() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockEnabledStorage.get()).thenReturn(false).thenReturn(false).thenReturn(true)

        geofenceInternal.isEnabled() shouldBe false

        geofenceInternal.enable(null)

        verify(mockEnabledStorage, times(1)).set(true)

        geofenceInternal.isEnabled() shouldBe true

    }

    @Test
    fun testIsEnabled_shouldReturnFalse_whenGeofenceIsDisabled() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockEnabledStorage.get()).thenReturn(true).thenReturn(false)

        geofenceInternal.isEnabled() shouldBe true

        geofenceInternalWithMockContext.disable()
        verify(mockEnabledStorage, times(1)).set(false)

        geofenceInternal.isEnabled() shouldBe false

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
        verify(mockFusedLocationProviderClient).lastLocation
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted_withCompletionListener() {
        val latch = CountDownLatch(1)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        var completionListenerHasBeenCalled = false

        geofenceInternal.enable {
            it shouldBe null
            completionListenerHasBeenCalled = true
            latch.countDown()
        }
        latch.await()
        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verify(mockFusedLocationProviderClient).lastLocation
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenFineLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(FakeLocationTask(mockLocation))
        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable {
            it is MissingPermissionException
            it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION"
            completionListenerHasBeenCalled = true
        }

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        verifyZeroInteractions(mockFusedLocationProviderClient)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenBackgroundLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(FakeLocationTask(mockLocation))
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
            verifyZeroInteractions(mockFusedLocationProviderClient)
            verifyZeroInteractions(mockGeofenceFilter)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenPermissionsDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(FakeLocationTask(mockLocation))
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
        verifyZeroInteractions(mockFusedLocationProviderClient)
        verifyZeroInteractions(mockGeofenceFilter)
    }

    @Test
    fun testEnable_registersGeofencesWithAdditionalRefreshArea() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.3)

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(geofenceResponse)

        val spyGeofenceInternal: GeofenceInternal = spy(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        whenever(mockGeofenceFilter.findNearestGeofences(any(), any())).thenReturn(nearestGeofencesWithoutRefreshArea)
        val currentLocation = (Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        })

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(FakeLocationTask(currentLocation))

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

    @Test
    fun testOnGeofenceTriggered() {
        val latch = CountDownLatch(1)
        val mockAction: Runnable = mock()
        val appEventAction = JSONObject("""
            {
                    "type": "MEAppEvent",
                    "name": "nameValue",
                    "payload": {
                      "someKey": "someValue"
                    }
                }
        """.trimIndent())

        val testTrigger = Trigger(id = "appEventActionId", type = TriggerType.ENTER, action = appEventAction)
        val trigger = Trigger(id = "triggerId", type = TriggerType.ENTER, action = JSONObject())
        val allGeofences = listOf(
                MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                MEGeofence("testId", 47.493827, 19.060715, 10.0, null, listOf(testTrigger)),
                MEGeofence("geofenceId4", 47.489680, 19.061230, 350.0, null, listOf(trigger)),
                MEGeofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )

        ReflectionTestUtils.setInstanceField(geofenceInternal, "nearestGeofences", allGeofences)
        whenever(mockAction.run()).thenAnswer { latch.countDown() }
        whenever(mockActionCommandFactory.createActionCommand(appEventAction)).thenReturn(mockAction)

        geofenceInternal.onGeofenceTriggered(listOf(TriggeringGeofence("testId", TriggerType.ENTER)))

        verify(mockActionCommandFactory).createActionCommand(appEventAction)

        latch.await()

        verify(mockAction).run()
    }

    @Test
    fun testOnGeofenceTriggered_onRefreshArea_recalculateGeofences() {
        val spyGeofenceInternal = spy(geofenceInternal)
        val latch = CountDownLatch(1)
        val mockAction: Runnable = mock()
        val appEventAction = JSONObject("""
            {
                    "type": "MEAppEvent",
                    "name": "nameValue",
                    "payload": {
                      "someKey": "someValue"
                    }
                }
        """.trimIndent())

        val testTrigger = Trigger(id = "appEventActionId", type = TriggerType.ENTER, action = appEventAction)
        val trigger = Trigger(id = "triggerId", type = TriggerType.ENTER, action = JSONObject())
        val currentLocation = (Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        })
        val allGeofences = listOf(
                MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                MEGeofence("testId", 47.493827, 19.060715, 10.0, null, listOf(testTrigger)),
                MEGeofence("geofenceId4", 47.489680, 19.061230, 350.0, null, listOf(trigger))
        )
        val geofenceResponse = GeofenceResponse(listOf(GeofenceGroup("group1", null, allGeofences), GeofenceGroup("group2", null, listOf(MEGeofence("geofenceId6", 47.492292, 19.056440, 10.0, null, listOf(trigger))))))
        val refreshArea = MEGeofence("refreshArea", 47.493160, 19.058355, 36.63777160644531, null, listOf(refreshTrigger))

        val nearestGeofences1 = listOf(
                MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(testTrigger)),
                MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                MEGeofence("refreshArea", 47.492292, 19.056440, 400.0, null, listOf(trigger))

        )

        val nearestGeofences2 = listOf(
                MEGeofence("testId", 47.493812, 19.058537, 0.5, null, listOf(testTrigger))
        )

        ReflectionTestUtils.setInstanceField(spyGeofenceInternal, "nearestGeofences", nearestGeofences1)
        ReflectionTestUtils.setInstanceField(spyGeofenceInternal, "geofenceResponse", geofenceResponse)
        whenever(mockAction.run()).thenAnswer { latch.countDown() }
        whenever(mockActionCommandFactory.createActionCommand(appEventAction)).thenReturn(mockAction)

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(FakeLocationTask(currentLocation))
        whenever(mockGeofenceFilter.findNearestGeofences(currentLocation, geofenceResponse)).thenReturn(nearestGeofences2)

        spyGeofenceInternal.onGeofenceTriggered(listOf(TriggeringGeofence("geofenceId1", TriggerType.ENTER), TriggeringGeofence("refreshArea", TriggerType.EXIT)))

        verify(mockActionCommandFactory).createActionCommand(appEventAction)

        latch.await()

        verify(mockAction).run()
        verify(mockGeofenceFilter).findNearestGeofences(currentLocation, geofenceResponse)
        argumentCaptor<List<MEGeofence>>().apply {
            verify(spyGeofenceInternal).registerGeofences(capture())
            allValues[0].size shouldBe 2
            allValues[0][1].toString() shouldBe refreshArea.toString()
        }
    }

    @Test
    fun testSetEventHandler() {
        val mockEventHandler: EventHandler = mock()
        geofenceInternal.setEventHandler(mockEventHandler)

        verify(mockEventHandlerProvider).eventHandler = mockEventHandler
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