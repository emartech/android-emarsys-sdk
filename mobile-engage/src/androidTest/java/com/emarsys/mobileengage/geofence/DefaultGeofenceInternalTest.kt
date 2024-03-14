package com.emarsys.mobileengage.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.mobileengage.api.geofence.Trigger
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.fake.FakeRequestManager
import com.emarsys.mobileengage.geofence.model.GeofenceGroup
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.TriggeringEmarsysGeofence
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.mockito.whenever
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.util.concurrent.CountDownLatch
import com.emarsys.mobileengage.api.geofence.Geofence as MEGeofence

class DefaultGeofenceInternalTest : AnnotationSpec() {

    private companion object {
        val refreshTrigger = Trigger(
            id = "refreshAreaTriggerId",
            type = TriggerType.EXIT,
            loiteringDelay = 0,
            action = JSONObject()
        )
        val trigger =
            Trigger(id = "refreshAreaTriggerId", type = TriggerType.ENTER, action = JSONObject())
        val refreshArea = MEGeofence(
            "refreshArea",
            47.493160,
            19.058355,
            49.079566955566406,
            null,
            listOf(refreshTrigger)
        )
        val nearestGeofencesWithoutRefreshArea: List<MEGeofence> = listOf(
            MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
            MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
            MEGeofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )

        val nearestGeofencesWithRefreshArea: List<MEGeofence> = listOf(
            MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
            MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
            MEGeofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger)),
            refreshArea
        )
    }


    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockGeofenceRequestModel: RequestModel
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var fakeRequestManager: RequestManager
    private lateinit var mockFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mockGeofenceResponseMapper: GeofenceResponseMapper
    private lateinit var geofenceInternal: GeofenceInternal
    private lateinit var mockPermissionChecker: PermissionChecker
    private lateinit var mockGeofenceFilter: GeofenceFilter
    private lateinit var mockLocation: Location
    private lateinit var mockGeofencingClient: GeofencingClient
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockContext: Context
    private lateinit var mockCacheableEventHandler: CacheableEventHandler
    private lateinit var geofenceInternalWithMockContext: GeofenceInternal
    private lateinit var mockEnabledStorage: Storage<Boolean>
    private lateinit var mockInitialEnterTriggerEnabledStorage: Storage<Boolean?>
    private lateinit var mockPendingIntentProvider: GeofencePendingIntentProvider
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var pendingIntent: PendingIntent
    private lateinit var mockTask: Task<Void>

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockInitialEnterTriggerEnabledStorage = mock()

        whenever(mockInitialEnterTriggerEnabledStorage.get()).thenReturn(false)

        mockResponseModel = mock()
        mockGeofenceRequestModel = mock()
        mockRequestModelFactory = mock()
        whenever(mockRequestModelFactory.createFetchGeofenceRequest()).thenReturn(
            mockGeofenceRequestModel
        )

        mockContext = mock()
        val context: Context = getInstrumentation().targetContext
        whenever(mockContext.packageName).thenReturn(
            "com.emarsys.mobileengage.test"
        )
        if (!AndroidVersionUtils.isBelow30) {
            whenever(mockContext.attributionTag).thenReturn("tag")
        }

        val intent = Intent("com.emarsys.sdk.GEOFENCE_ACTION")
        pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        mockPendingIntentProvider = mock()
        whenever(mockPendingIntentProvider.providePendingIntent()).thenReturn(pendingIntent)


        fakeRequestManager =
            spy(FakeRequestManager(FakeRequestManager.ResponseType.SUCCESS, mockResponseModel))

        mockGeofenceResponseMapper = mock()
        whenever(
            mockGeofenceResponseMapper.map(any())
        ).thenReturn(GeofenceResponse(listOf()))

        mockPermissionChecker = mock()
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_DENIED
        )


        mockLocation = mock()

        mockTask = mock()
        argumentCaptor<OnCompleteListener<Void>> {
            whenever(mockTask.addOnCompleteListener(capture())).thenAnswer {
                firstValue.onComplete(mockTask)
                mockTask
            }
        }


        mockFusedLocationProviderClient = mock()
        whenever(
            mockFusedLocationProviderClient.requestLocationUpdates(
                any(),
                any()
            )
        ).thenReturn(mockTask)

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(mockLocation)
        )
        mockGeofenceFilter = mock()

        whenever(
            mockGeofenceFilter.findNearestGeofences(
                any(),
                any()
            )
        ).thenReturn(nearestGeofencesWithRefreshArea)

        mockLocation = mock()
        whenever(mockLocation.latitude).thenReturn(47.493165)
        whenever(mockLocation.longitude).thenReturn(19.058359)

        mockGeofencingClient = mock()

        mockActionCommandFactory = mock()
        mockCacheableEventHandler = mock()
        mockEnabledStorage = mock()

        whenever(mockEnabledStorage.get()).thenReturn(true)


        geofenceInternal = DefaultGeofenceInternal(
            mockRequestModelFactory,
            fakeRequestManager,
            mockGeofenceResponseMapper,
            mockPermissionChecker,
            mockFusedLocationProviderClient,
            mockGeofenceFilter,
            mockGeofencingClient,
            mockContext,
            mockActionCommandFactory,
            mockCacheableEventHandler,
            mockEnabledStorage,
            mockPendingIntentProvider,
            concurrentHandlerHolder,
            mockInitialEnterTriggerEnabledStorage
        )

        geofenceInternalWithMockContext = DefaultGeofenceInternal(
            mockRequestModelFactory,
            fakeRequestManager,
            mockGeofenceResponseMapper,
            mockPermissionChecker,
            mockFusedLocationProviderClient,
            mockGeofenceFilter,
            mockGeofencingClient,
            mockContext,
            mockActionCommandFactory,
            mockCacheableEventHandler,
            mockEnabledStorage,
            mockPendingIntentProvider,
            concurrentHandlerHolder,
            mockInitialEnterTriggerEnabledStorage
        )
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

        verify(
            mockContext,
            timeout(100).times(1)
        ).registerReceiver(any<GeofenceBroadcastReceiver>(), any())

    }

    @Test
    fun testDisable_unregistersGeofenceBroadcastReceiver() {
        geofenceInternalWithMockContext.enable(null)
        geofenceInternalWithMockContext.disable()

        verify(mockContext).unregisterReceiver(any<GeofenceBroadcastReceiver>())
        verify(mockFusedLocationProviderClient).removeLocationUpdates(pendingIntent)
    }

    @Test
    fun testDisable_shouldNotCallUnregisterReceiver_ifReceiversAreNotRegistered() {
        geofenceInternalWithMockContext.enable(null)
        geofenceInternalWithMockContext.disable()
        geofenceInternalWithMockContext.disable()

        verify(
            mockContext,
            timeout(100).times(1)
        ).unregisterReceiver(any<GeofenceBroadcastReceiver>())

    }

    @Test
    fun testDisable_shouldNotCrash_whenUnregisterReceiverCalled_multipleTimes() {
        geofenceInternal.enable(null)
        geofenceInternal.disable()
        ReflectionTestUtils.setInstanceField(
            geofenceInternalWithMockContext,
            "receiverRegistered",
            true
        )
        geofenceInternal.disable()
    }

    @Test
    fun testDisable() {
        geofenceInternalWithMockContext.enable(null)
        geofenceInternalWithMockContext.disable()
        geofenceInternalWithMockContext.enable(null)

        verify(
            mockContext, timeout(100).times(2)
        ).registerReceiver(any<GeofenceBroadcastReceiver>(), any())

    }

    @Test
    fun testFetch_doNotFetch_whenGeofenceIsNotEnabled() {
        whenever(mockEnabledStorage.get()).thenReturn(false)

        geofenceInternal.fetchGeofences(null)

        verify(mockRequestModelFactory, times(0)).createFetchGeofenceRequest()
        verify(fakeRequestManager, times(0)).submit(any(), any())
    }

    @Test
    fun testFetch_fetch_whenGeofenceIsEnabled() {
        whenever(mockEnabledStorage.get()).thenReturn(
            true
        )

        geofenceInternal.fetchGeofences(null)

        verify(mockRequestModelFactory).createFetchGeofenceRequest()
        verify(fakeRequestManager).submitNow(eq(mockGeofenceRequestModel), any())

    }

    @Test
    fun testEnable_shouldSetEnabledStorage_andFetchIfWasDisabled() {
        val latch = CountDownLatch(1)
        var completionListenerHasBeenCalled = false
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

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
        geofenceInternalWithMockContext.enable(null)
        geofenceInternalWithMockContext.disable()

        verify(mockEnabledStorage).set(false)

    }

    @Test
    fun testIsEnabled_shouldReturnTrue_whenGeofenceIsEnabled() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockEnabledStorage.get()).thenReturn(false).thenReturn(false).thenReturn(true)

        geofenceInternal.isEnabled() shouldBe false

        geofenceInternal.enable(null)

        verify(mockEnabledStorage, timeout(100)).set(true)

        geofenceInternal.isEnabled() shouldBe true
    }

    @Test
    fun testIsEnabled_shouldReturnFalse_whenGeofenceIsDisabled() {
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockEnabledStorage.get()).thenReturn(true).thenReturn(true).thenReturn(false)

        geofenceInternal.isEnabled() shouldBe true
        geofenceInternal.disable()
        verify(mockEnabledStorage, timeout(100)).set(false)

        geofenceInternal.isEnabled() shouldBe false
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted() {
        whenever(
            mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        geofenceInternal.enable(null)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            verify(mockFusedLocationProviderClient).lastLocation
        }
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted_withCompletionListener() {
        val latch = CountDownLatch(1)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

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
    fun testEnable_returnDoNoTReturn_PermissionForLocationException_whenFineLocationPermissionDenied_andCoarseLocationGranted() {
        val latch = CountDownLatch(1)
        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_DENIED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

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

        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        verify(mockFusedLocationProviderClient).lastLocation
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenFineLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_DENIED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(
                mockLocation
            )
        )

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(
            geofenceResponse
        )

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable {
            it is MissingPermissionException
            it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION"
            completionListenerHasBeenCalled = true
        }

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        verifyNoInteractions(
            mockFusedLocationProviderClient
        )

        verifyNoInteractions(mockGeofenceFilter)
    }


    @Test
    fun testEnable_returnNoPermissionForLocationException_whenBackgroundLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_DENIED
        )

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(
                mockLocation
            )
        )

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(
            geofenceResponse
        )

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable {
            it is MissingPermissionException
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it?.message shouldBe "Couldn't acquire permission for ACCESS_BACKGROUND_LOCATION"
            }
            completionListenerHasBeenCalled = true
        }

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            verifyNoInteractions(mockFusedLocationProviderClient)
            verifyNoInteractions(mockGeofenceFilter)
        }

        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenPermissionsDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_DENIED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_DENIED
        )

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(
                mockLocation
            )
        )

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(
            geofenceResponse
        )

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable {
            it is MissingPermissionException
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION and ACCESS_BACKGROUND_LOCATION"
            } else {
                it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION"
            }
            completionListenerHasBeenCalled = true
        }

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        verify(mockPermissionChecker).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        verifyNoInteractions(
            mockFusedLocationProviderClient
        )

        verifyNoInteractions(mockGeofenceFilter)

    }

    @Test
    fun testEnable_registersGeofencesWithAdditionalRefreshArea() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.3)

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(
            geofenceResponse
        )

        val spyGeofenceInternal: GeofenceInternal = spy(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        whenever(mockGeofenceFilter.findNearestGeofences(any(), any())).thenReturn(
            nearestGeofencesWithoutRefreshArea
        )

        val currentLocation = Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        }

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(
                currentLocation
            )
        )

        spyGeofenceInternal.enable(null)

        argumentCaptor<List<MEGeofence>> {
            verify(spyGeofenceInternal, times(2)).registerGeofences(capture())

            lastValue.size shouldBe 4
            lastValue[3].toString() shouldBe refreshArea.toString()
        }
    }

    @Test
    fun testEnable_shouldNotCrash_registersGeofencesWhenRefreshRadiusWouldBeNegative() {
        val geofenceResponse = GeofenceResponse(listOf(), 1.0)

        whenever(mockGeofenceResponseMapper.map(any())).thenReturn(
            geofenceResponse
        )

        val spyGeofenceInternal: GeofenceInternal = spy(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        whenever(mockGeofenceFilter.findNearestGeofences(any(), any())).thenReturn(
            listOf(nearestGeofencesWithoutRefreshArea[0])
        )

        val currentLocation = (Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        })

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(currentLocation)
        )

        spyGeofenceInternal.enable(null)
    }

    @Test
    fun testRegisterGeofences_geofencingClientAddsNearestGeofences() {
        val geofencesToTest =
            nearestGeofencesWithoutRefreshArea.map { createGeofence(it) } + createGeofence(
                refreshArea
            )
        val geofencingRequest =
            GeofencingRequest.Builder().addGeofences(geofencesToTest).setInitialTrigger(0)
                .build()
        argumentCaptor<GeofencingRequest> {
            whenever(
                mockGeofencingClient.addGeofences(
                    capture(),
                    any()
                )
            ).thenReturn(
                mockTask
            )

            geofenceInternal.registerGeofences(nearestGeofencesWithoutRefreshArea + refreshArea)

            firstValue.initialTrigger shouldBe geofencingRequest.initialTrigger
            firstValue.geofences.forEachIndexed { index, geofence ->
                geofence.requestId shouldBe geofencingRequest.geofences[index].requestId
            }
        }

    }

    @Test
    fun testRegisterGeofences_geofencingRequest_shouldIncludeInitialEnterTrigger() {
        val geofencesToTest =
            nearestGeofencesWithoutRefreshArea.map { createGeofence(it) } + createGeofence(
                Companion.refreshArea
            )
        val geofencingRequest =
            GeofencingRequest.Builder().addGeofences(geofencesToTest).build()
        argumentCaptor<GeofencingRequest> {
            whenever(
                mockGeofencingClient.addGeofences(
                    capture(),
                    any()
                )
            ).thenReturn(
                mockTask
            )


            geofenceInternal.setInitialEnterTriggerEnabled(true)
            geofenceInternal.registerGeofences(nearestGeofencesWithoutRefreshArea + refreshArea)

            verify(mockInitialEnterTriggerEnabledStorage).set(true)
            firstValue.initialTrigger shouldBe GeofencingRequest.INITIAL_TRIGGER_ENTER
            firstValue.geofences.forEachIndexed { index, geofence ->
                geofence.requestId shouldBe geofencingRequest.geofences[index].requestId
            }
        }
    }

    @Test
    fun testOnGeofenceTriggered() {
        val latch = CountDownLatch(1)
        val mockAction: Runnable = mock()
        val appEventAction = JSONObject(
            """
        {
                "type": "MEAppEvent",
                "name": "nameValue",
                "payload": {
                  "someKey": "someValue"
                }
            }
    """.trimIndent()
        )
        val testTrigger =
            Trigger(id = "appEventActionId", type = TriggerType.ENTER, action = appEventAction)
        val testExitTrigger =
            Trigger(id = "appEventActionId", type = TriggerType.EXIT, action = appEventAction)
        val trigger = Trigger(id = "triggerId", type = TriggerType.ENTER, action = JSONObject())
        val allGeofences = listOf(
            MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
            MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
            MEGeofence(
                "testId",
                47.493827,
                19.060715,
                10.0,
                null,
                listOf(testTrigger, testExitTrigger)
            ),
            MEGeofence("geofenceId4", 47.489680, 19.061230, 350.0, null, listOf(trigger)),
            MEGeofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )
        ReflectionTestUtils.setInstanceField(geofenceInternal, "nearestGeofences", allGeofences)
        whenever(mockAction.run()).thenAnswer { latch.countDown() }
        whenever(mockActionCommandFactory.createActionCommand(appEventAction)).thenReturn(
            mockAction
        )
        geofenceInternal.onGeofenceTriggered(
            listOf(
                TriggeringEmarsysGeofence(
                    "testId",
                    TriggerType.ENTER
                )
            )
        )

        verify(
            mockActionCommandFactory, times(1)
        ).createActionCommand(
            appEventAction
        )

        latch.await()
        verify(mockAction).run()
    }


    @Test
    fun testOnGeofenceTriggered_onRefreshArea_recalculateGeofences() {
        val spyGeofenceInternal = spy(geofenceInternal)
        val latch = CountDownLatch(1)
        val mockAction: Runnable = mock()
        val appEventAction = JSONObject(
            """
            {
                    "type": "MEAppEvent",
                    "name": "nameValue",
                    "payload": {
                      "someKey": "someValue"
                    }
                }
        """.trimIndent()
        )

        val testTrigger =
            Trigger(id = "appEventActionId", type = TriggerType.ENTER, action = appEventAction)
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
        val geofenceResponse = GeofenceResponse(
            listOf(
                GeofenceGroup("group1", null, allGeofences),
                GeofenceGroup(
                    "group2",
                    null,
                    listOf(
                        MEGeofence(
                            "geofenceId6",
                            47.492292,
                            19.056440,
                            10.0,
                            null,
                            listOf(trigger)
                        )
                    )
                )
            )
        )
        val refreshArea = MEGeofence(
            "refreshArea",
            47.493160,
            19.058355,
            36.63777160644531,
            null,
            listOf(refreshTrigger)
        )

        val nearestGeofences1 = listOf(
            MEGeofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(testTrigger)),
            MEGeofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
            MEGeofence("refreshArea", 47.492292, 19.056440, 400.0, null, listOf(trigger))

        )

        val nearestGeofences2 = listOf(
            MEGeofence("testId", 47.493812, 19.058537, 0.5, null, listOf(testTrigger))
        )

        ReflectionTestUtils.setInstanceField(
            spyGeofenceInternal,
            "nearestGeofences",
            nearestGeofences1
        )
        ReflectionTestUtils.setInstanceField(
            spyGeofenceInternal,
            "geofenceResponse",
            geofenceResponse
        )
        whenever(mockAction.run()).thenAnswer { latch.countDown() }
        whenever(mockActionCommandFactory.createActionCommand(appEventAction)).thenReturn(
            mockAction
        )

        whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(
            FakeLocationTask(currentLocation)
        )

        whenever(
            mockGeofenceFilter.findNearestGeofences(
                currentLocation,
                geofenceResponse
            )
        ).thenReturn(
            nearestGeofences2
        )

        spyGeofenceInternal.onGeofenceTriggered(
            listOf(
                TriggeringEmarsysGeofence(
                    "geofenceId1",
                    TriggerType.ENTER
                ), TriggeringEmarsysGeofence("refreshArea", TriggerType.EXIT)
            )
        )

        argumentCaptor<List<MEGeofence>> {
            verify(spyGeofenceInternal).registerGeofences(capture())
            verify(mockActionCommandFactory).createActionCommand(appEventAction)

            latch.await()

            verify(mockAction).run()

            verify(
                mockGeofenceFilter
            ).findNearestGeofences(
                currentLocation,
                geofenceResponse
            )

            firstValue.size shouldBe 2
            firstValue[1].toString() shouldBe refreshArea.toString()
        }
    }

    private fun createGeofence(geofence: MEGeofence): Geofence {
        return Geofence.Builder()
            .setRequestId(geofence.id)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Long.MAX_VALUE)
            .setCircularRegion(geofence.lon, geofence.lat, geofence.radius.toFloat())
            .build()
    }
}