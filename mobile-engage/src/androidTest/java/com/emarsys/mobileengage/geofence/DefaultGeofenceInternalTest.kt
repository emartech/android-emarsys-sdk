import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.api.geofence.Trigger
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.fake.FakeRequestManager
import com.emarsys.mobileengage.geofence.DefaultGeofenceInternal
import com.emarsys.mobileengage.geofence.FakeLocationTask
import com.emarsys.mobileengage.geofence.GeofenceFilter
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.geofence.GeofencePendingIntentProvider
import com.emarsys.mobileengage.geofence.GeofenceResponseMapper
import com.emarsys.mobileengage.geofence.model.GeofenceGroup
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.TriggeringEmarsysGeofence
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.ReflectionTestUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import com.emarsys.mobileengage.api.geofence.Geofence as MEGeofence

class DefaultGeofenceInternalTest {

    private companion object {
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
    private lateinit var mockCacheableEventHandler: CacheableEventHandler
    private lateinit var mockEnabledStorage: Storage<Boolean>
    private lateinit var mockInitialEnterTriggerEnabledStorage: Storage<Boolean?>
    private lateinit var mockPendingIntentProvider: GeofencePendingIntentProvider
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var pendingIntent: PendingIntent
    private lateinit var mockTask: Task<Void>

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockInitialEnterTriggerEnabledStorage = mockk(relaxed = true)

        every { mockInitialEnterTriggerEnabledStorage.get() } returns false

        mockResponseModel = mockk(relaxed = true)
        mockGeofenceRequestModel = mockk(relaxed = true)
        mockRequestModelFactory = mockk(relaxed = true)
        every { mockRequestModelFactory.createFetchGeofenceRequest() } returns mockGeofenceRequestModel

        mockPendingIntentProvider = mockk(relaxed = true)
        pendingIntent = mockk(relaxed = true)
        every { mockPendingIntentProvider.providePendingIntent() } returns pendingIntent

        fakeRequestManager =
            spyk(FakeRequestManager(FakeRequestManager.ResponseType.SUCCESS, mockResponseModel))

        mockGeofenceResponseMapper = mockk(relaxed = true)
        every { mockGeofenceResponseMapper.map(any()) } returns GeofenceResponse(listOf())

        mockPermissionChecker = mockk(relaxed = true)
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_DENIED

        mockLocation = mockk(relaxed = true)

        mockTask = mockk(relaxed = true)
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void>>().onComplete(mockTask)
            mockTask
        }

        mockFusedLocationProviderClient = mockk(relaxed = true)
        every {
            mockFusedLocationProviderClient.requestLocationUpdates(
                any(),
                any()
            )
        } returns mockTask
        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(mockLocation)

        mockGeofenceFilter = mockk(relaxed = true)
        every {
            mockGeofenceFilter.findNearestGeofences(
                any(),
                any()
            )
        } returns nearestGeofencesWithRefreshArea

        mockLocation = mockk(relaxed = true)
        every { mockLocation.latitude } returns 47.493165
        every { mockLocation.longitude } returns 19.058359

        mockGeofencingClient = mockk(relaxed = true)

        mockActionCommandFactory = mockk(relaxed = true)
        mockCacheableEventHandler = mockk(relaxed = true)
        mockEnabledStorage = mockk(relaxed = true)
        every { mockEnabledStorage.get() } returns true

        geofenceInternal = DefaultGeofenceInternal(
            mockRequestModelFactory,
            fakeRequestManager,
            mockGeofenceResponseMapper,
            mockPermissionChecker,
            mockFusedLocationProviderClient,
            mockGeofenceFilter,
            mockGeofencingClient,
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

        verify { fakeRequestManager.submitNow(any(), any()) }
    }

    @Test
    fun testFetchGeofences_callsMapOnResponseMapper_onSuccess() {
        geofenceInternal.fetchGeofences(null)

        verify { mockGeofenceResponseMapper.map(mockResponseModel) }
    }

    @Test
    fun testFetchGeofences_callsEnable_onSuccess() {
        val spyGeofenceInternal = spyk(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        verify { spyGeofenceInternal.enable(null) }
    }

    @Test
    fun testEnable_checksForLocationPermissions_throughPermissionChecker() {
        geofenceInternal.enable(null)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
        }
        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
    }

    @Test
    fun testFetch_doNotFetch_whenGeofenceIsNotEnabled() {
        every { mockEnabledStorage.get() } returns false

        geofenceInternal.fetchGeofences(null)

        verify(exactly = 0) { mockRequestModelFactory.createFetchGeofenceRequest() }
        verify(exactly = 0) { fakeRequestManager.submit(any(), any()) }
    }

    @Test
    fun testFetch_fetch_whenGeofenceIsEnabled() {
        every { mockEnabledStorage.get() } returns true

        geofenceInternal.fetchGeofences(null)

        verify { mockRequestModelFactory.createFetchGeofenceRequest() }
        verify { fakeRequestManager.submitNow(eq(mockGeofenceRequestModel), any()) }
    }

    @Test
    fun testEnable_shouldSetEnabledStorage_andFetchIfWasDisabled() {
        val latch = CountDownLatch(1)
        var completionListenerHasBeenCalled = false
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        every { mockEnabledStorage.get() } returns false andThen true

        geofenceInternal.enable {
            it shouldBe null
            completionListenerHasBeenCalled = true
            latch.countDown()
        }

        latch.await()
        completionListenerHasBeenCalled shouldBe true
        verify { mockEnabledStorage.set(true) }
        verify { mockRequestModelFactory.createFetchGeofenceRequest() }
    }

    @Test
    fun testDisable_shouldClearEnabledStorage() {
        geofenceInternal.enable(null)
        geofenceInternal.disable()

        verify { mockEnabledStorage.set(false) }
    }

    @Test
    fun testIsEnabled_shouldReturnTrue_whenGeofenceIsEnabled() {
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockEnabledStorage.get() } returns false andThen false andThen true

        geofenceInternal.isEnabled() shouldBe false

        geofenceInternal.enable(null)

        verify { mockEnabledStorage.set(true) }

        geofenceInternal.isEnabled() shouldBe true
    }

    @Test
    fun testIsEnabled_shouldReturnFalse_whenGeofenceIsDisabled() {
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockEnabledStorage.get() } returns true andThen true andThen false

        geofenceInternal.isEnabled() shouldBe true
        geofenceInternal.disable()
        verify { mockEnabledStorage.set(false) }

        geofenceInternal.isEnabled() shouldBe false
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted() {
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        geofenceInternal.enable(null)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
            verify { mockFusedLocationProviderClient.lastLocation }
        }
    }

    @Test
    fun testEnable_fetchLastKnownLocation_fromLocationManager_whenPermissionGranted_withCompletionListener() {
        val latch = CountDownLatch(1)
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        var completionListenerHasBeenCalled = false

        geofenceInternal.enable {
            it shouldBe null
            completionListenerHasBeenCalled = true
            latch.countDown()
        }
        latch.await()
        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
        }

        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
        verify { mockFusedLocationProviderClient.lastLocation }
    }

    @Test
    fun testEnable_returnDoNoTReturn_PermissionForLocationException_whenFineLocationPermissionDenied_andCoarseLocationGranted() {
        val latch = CountDownLatch(1)
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        var completionListenerHasBeenCalled = false

        geofenceInternal.enable {
            it shouldBe null
            completionListenerHasBeenCalled = true
            latch.countDown()
        }
        latch.await()
        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
        }

        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) }
        verify { mockFusedLocationProviderClient.lastLocation }
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenFineLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(mockLocation)
        every { mockGeofenceResponseMapper.map(any()) } returns geofenceResponse

        var completionListenerHasBeenCalled = false
        geofenceInternal.enable {
            it is MissingPermissionException
            it?.message shouldBe "Couldn't acquire permission for ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION"
            completionListenerHasBeenCalled = true
        }

        completionListenerHasBeenCalled shouldBe true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
        }

        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }

        confirmVerified(mockFusedLocationProviderClient)
        confirmVerified(mockGeofenceFilter)
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenBackgroundLocationPermissionDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(mockLocation)
        every { mockGeofenceResponseMapper.map(any()) } returns geofenceResponse

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
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
            confirmVerified(mockFusedLocationProviderClient)
            confirmVerified(mockGeofenceFilter)
        }

        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
    }

    @Test
    fun testEnable_returnNoPermissionForLocationException_whenPermissionsDenied() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.0)

        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(mockLocation)
        every { mockGeofenceResponseMapper.map(any()) } returns geofenceResponse

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
            verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
        }
        verify { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) }

        confirmVerified(mockFusedLocationProviderClient)
        confirmVerified(mockGeofenceFilter)
    }

    @Test
    fun testEnable_registersGeofencesWithAdditionalRefreshArea() {
        val geofenceResponse = GeofenceResponse(listOf(), 0.3)

        every { mockGeofenceResponseMapper.map(any()) } returns geofenceResponse

        val spyGeofenceInternal: GeofenceInternal = spyk(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        every {
            mockGeofenceFilter.findNearestGeofences(
                any(),
                any()
            )
        } returns nearestGeofencesWithoutRefreshArea

        val currentLocation = Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        }

        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(
            currentLocation
        )

        spyGeofenceInternal.enable(null)

        val slot = mutableListOf<List<MEGeofence>>()
        verify(exactly = 2) { spyGeofenceInternal.registerGeofences(capture(slot)) }
        slot.size shouldBe 2

        slot[1].size shouldBe 4
        slot[1][3].toString() shouldBe refreshArea.toString()
    }

    @Test
    fun testEnable_shouldNotCrash_registersGeofencesWhenRefreshRadiusWouldBeNegative() {
        val geofenceResponse = GeofenceResponse(listOf(), 1.0)

        every { mockGeofenceResponseMapper.map(any()) } returns geofenceResponse

        val spyGeofenceInternal: GeofenceInternal = spyk(geofenceInternal)
        spyGeofenceInternal.fetchGeofences(null)

        every { mockGeofenceFilter.findNearestGeofences(any(), any()) } returns listOf(
            nearestGeofencesWithoutRefreshArea[0]
        )

        val currentLocation = Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        }

        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockPermissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(
            currentLocation
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
            GeofencingRequest.Builder().addGeofences(geofencesToTest).setInitialTrigger(0).build()

        val slot = slot<GeofencingRequest>()
        every { mockGeofencingClient.addGeofences(capture(slot), any()) } returns mockTask

        geofenceInternal.registerGeofences(nearestGeofencesWithoutRefreshArea + refreshArea)

        slot.captured.initialTrigger shouldBe geofencingRequest.initialTrigger
        slot.captured.geofences.forEachIndexed { index, geofence ->
            geofence.requestId shouldBe geofencingRequest.geofences[index].requestId
        }
    }

    @Test
    fun testRegisterGeofences_geofencingRequest_shouldIncludeInitialEnterTrigger() {
        val geofencesToTest =
            nearestGeofencesWithoutRefreshArea.map { createGeofence(it) } + createGeofence(
                refreshArea
            )
        val geofencingRequest = GeofencingRequest.Builder().addGeofences(geofencesToTest).build()

        val slot = slot<GeofencingRequest>()
        every { mockGeofencingClient.addGeofences(capture(slot), any()) } returns mockTask

        geofenceInternal.setInitialEnterTriggerEnabled(true)
        geofenceInternal.registerGeofences(nearestGeofencesWithoutRefreshArea + refreshArea)

        verify { mockInitialEnterTriggerEnabledStorage.set(true) }
        slot.captured.initialTrigger shouldBe GeofencingRequest.INITIAL_TRIGGER_ENTER
        slot.captured.geofences.forEachIndexed { index, geofence ->
            geofence.requestId shouldBe geofencingRequest.geofences[index].requestId
        }
    }

    @Test
    fun testOnGeofenceTriggered_shouldExecuteActions_ifFeatureIsEnabled_andNearestGeofences_isNotEmpty() {
        val latch = CountDownLatch(1)
        val mockAction: Runnable = mockk(relaxed = true)
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
        every { mockAction.run() } answers { latch.countDown() }
        every { mockActionCommandFactory.createActionCommand(appEventAction) } returns mockAction

        every { mockEnabledStorage.get() } returns true

        geofenceInternal.onGeofenceTriggered(
            listOf(
                TriggeringEmarsysGeofence(
                    "testId",
                    TriggerType.ENTER
                )
            )
        )

        verify { mockActionCommandFactory.createActionCommand(appEventAction) }

        latch.await()
        verify { mockAction.run() }
    }

    @Test
    fun testOnGeofenceTriggered_shouldFetchFirst_andHandleActions_ifFeatureIsEnabled_andNearestGeofences_isEmpty() {
        every { mockEnabledStorage.get() } returns true
        ReflectionTestUtils.setInstanceField(
            geofenceInternal,
            "nearestGeofences",
            emptyList<MEGeofence>()
        )
        every { mockGeofenceResponseMapper.map(mockResponseModel) } returns GeofenceResponse(
            listOf(
                GeofenceGroup(
                    "group1", null, listOf(
                        com.emarsys.mobileengage.api.geofence.Geofence(
                            "geofenceId1", 47.493827, 19.060715, 10.0, null, listOf(
                                Trigger("triggerId", TriggerType.ENTER, 0, appEventAction)
                            )
                        )
                    )
                )
            )
        )
        geofenceInternal.onGeofenceTriggered(
            listOf(
                TriggeringEmarsysGeofence(
                    "geofenceId1",
                    TriggerType.ENTER
                )
            )
        )

        verify { mockGeofenceResponseMapper.map(mockResponseModel) }
        verify { mockActionCommandFactory.createActionCommand(any()) }
    }

    @Test
    fun testOnGeofenceTriggered_shouldNotHandleActions_ifFeatureIsDisabled() {
        every { mockEnabledStorage.get() } returns false

        geofenceInternal.onGeofenceTriggered(
            listOf(
                TriggeringEmarsysGeofence(
                    "testId",
                    TriggerType.ENTER
                )
            )
        )

        verify(exactly = 0) { mockActionCommandFactory.createActionCommand(any()) }
    }

    @Test
    fun testOnGeofenceTriggered_onRefreshArea_recalculateGeofences() {
        val spyGeofenceInternal = spyk(geofenceInternal)
        val latch = CountDownLatch(1)
        val mockAction: Runnable = mockk(relaxed = true)
        val testTrigger =
            Trigger(id = "appEventActionId", type = TriggerType.ENTER, action = appEventAction)
        val trigger = Trigger(id = "triggerId", type = TriggerType.ENTER, action = JSONObject())
        val currentLocation = Location(LocationManager.GPS_PROVIDER).apply {
            this.latitude = 47.493160
            this.longitude = 19.058355
        }
        every { mockEnabledStorage.get() } returns true
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
        every { mockAction.run() } answers { latch.countDown() }
        every { mockActionCommandFactory.createActionCommand(appEventAction) } returns mockAction

        every { mockFusedLocationProviderClient.lastLocation } returns FakeLocationTask(
            currentLocation
        )

        every {
            mockGeofenceFilter.findNearestGeofences(
                currentLocation,
                geofenceResponse
            )
        } returns nearestGeofences2

        val slot = slot<List<MEGeofence>>()
        spyGeofenceInternal.onGeofenceTriggered(
            listOf(
                TriggeringEmarsysGeofence("geofenceId1", TriggerType.ENTER),
                TriggeringEmarsysGeofence("refreshArea", TriggerType.EXIT)
            )
        )

        verify { spyGeofenceInternal.registerGeofences(capture(slot)) }
        verify { mockActionCommandFactory.createActionCommand(appEventAction) }

        latch.await()

        verify { mockAction.run() }

        verify { mockGeofenceFilter.findNearestGeofences(currentLocation, geofenceResponse) }

        slot.captured.size shouldBe 2
        slot.captured[1].toString() shouldBe refreshArea.toString()
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