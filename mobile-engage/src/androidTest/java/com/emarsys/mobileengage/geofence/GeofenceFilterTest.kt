package com.emarsys.mobileengage.geofence

import android.location.Location
import android.location.LocationManager
import com.emarsys.mobileengage.geofence.model.*
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class GeofenceFilterTest {

    private companion object {
        val currentLocation: Location = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 47.493160
            longitude = 19.058355
        }
        val trigger = Trigger(id = "triggerId", type = TriggerType.ENTER, action = JSONObject())
        val allGeofences = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10, null, listOf(trigger)),
                Geofence("geofenceId3", 47.493827, 19.060715, 10, null, listOf(trigger)),
                Geofence("geofenceId4", 47.489680, 19.061230, 350, null, listOf(trigger)),
                Geofence("geofenceId5", 47.492292, 19.056440, 10, null, listOf(trigger))
        )
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var geofenceFilter: GeofenceFilter

    @Before
    fun setUp() {
        geofenceFilter = GeofenceFilter(allGeofences.size)
    }


    @Test
    fun testFindNearestGeofences_shouldReturnListOfGeofences_whenMoreThanOneGroupIsTheGeofenceResponse() {
        val expected = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10, null, listOf(trigger)),
                Geofence("geofenceId4", 47.489680, 19.061230, 350, null, listOf(trigger)),
                Geofence("geofenceId5", 47.492292, 19.056440, 10, null, listOf(trigger)),
                Geofence("geofenceId6", 47.492292, 19.056440, 10, null, listOf(trigger)))
        val geofenceResponse = GeofenceResponse(listOf(GeofenceGroup("group1", null, allGeofences), GeofenceGroup("group2", null, listOf(Geofence("geofenceId6", 47.492292, 19.056440, 10, null, listOf(trigger))))))

        val result = geofenceFilter.findNearestGeofences(currentLocation, geofenceResponse)

        result shouldBe expected
    }

    @Test
    fun testFindNearestGeofences_shouldFilterNearestLocations_toCurrentLocation() {
        geofenceFilter = GeofenceFilter(3)

        val expected = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10, null, listOf(trigger)),
                Geofence("geofenceId4", 47.489680, 19.061230, 350, null, listOf(trigger)))

        val geofenceResponse = GeofenceResponse(listOf(GeofenceGroup("group1", null, allGeofences)))
        val result = geofenceFilter.findNearestGeofences(currentLocation, geofenceResponse)

        result shouldBe expected
    }
}