package com.emarsys.mobileengage.geofence

import android.location.Location
import android.location.LocationManager
import com.emarsys.mobileengage.api.geofence.Geofence
import com.emarsys.mobileengage.api.geofence.Trigger
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.geofence.model.GeofenceGroup
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test


class GeofenceFilterTest {

    private companion object {
        val currentLocation: Location = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 47.493160
            longitude = 19.058355
        }
        val trigger = Trigger(id = "triggerId", type = TriggerType.ENTER, action = JSONObject())
        val allGeofences = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                Geofence("geofenceId3", 47.493827, 19.060715, 10.0, null, listOf(trigger)),
                Geofence("geofenceId4", 47.489680, 19.061230, 350.0, null, listOf(trigger)),
                Geofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )
    }



    private lateinit var geofenceFilter: GeofenceFilter

    @BeforeEach
    fun setUp() {
        geofenceFilter = GeofenceFilter(allGeofences.size)
    }


    @Test
    fun testFindNearestGeofences_shouldReturnListOfGeofences_whenMoreThanOneGroupIsTheGeofenceResponse() {
        val expected = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                Geofence("geofenceId4", 47.489680, 19.061230, 350.0, null, listOf(trigger)),
                Geofence("geofenceId5", 47.492292, 19.056440, 10.0, null, listOf(trigger)),
                Geofence("geofenceId6", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        )
        val geofenceResponse = GeofenceResponse(listOf(GeofenceGroup("group1", null, allGeofences), GeofenceGroup("group2", null, listOf(
            Geofence("geofenceId6", 47.492292, 19.056440, 10.0, null, listOf(trigger))
        ))))

        val result = geofenceFilter.findNearestGeofences(currentLocation, geofenceResponse)

        result shouldBe expected
    }

    @Test
    fun testFindNearestGeofences_shouldFilterNearestLocations_toCurrentLocation() {
        geofenceFilter = GeofenceFilter(3)

        val expected = listOf(
                Geofence("geofenceId1", 47.493160, 19.058355, 10.0, null, listOf(trigger)),
                Geofence("geofenceId2", 47.493812, 19.058537, 10.0, null, listOf(trigger)),
                Geofence("geofenceId4", 47.489680, 19.061230, 350.0, null, listOf(trigger))
        )

        val geofenceResponse = GeofenceResponse(listOf(GeofenceGroup("group1", null, allGeofences)))
        val result = geofenceFilter.findNearestGeofences(currentLocation, geofenceResponse)

        result shouldBe expected
    }

    @Test
    fun testFindNearestGeofences_shouldUseGeofenceListSizeAsLimit_ifLesserThanLimit() {
        geofenceFilter = GeofenceFilter(30)

        val geofenceResponse = GeofenceResponse(listOf(GeofenceGroup("group1", null, allGeofences)))
        val result = geofenceFilter.findNearestGeofences(currentLocation, geofenceResponse)

        result.size shouldBe 5
    }
}