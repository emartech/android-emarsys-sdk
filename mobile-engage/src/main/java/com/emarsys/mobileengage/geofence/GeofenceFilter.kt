package com.emarsys.mobileengage.geofence

import android.location.Location
import android.location.LocationManager
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.api.geofence.Geofence
import com.emarsys.mobileengage.geofence.model.GeofenceResponse

@Mockable
class GeofenceFilter(private val limit: Int) {
    fun findNearestGeofences(currentLocation: Location, geofenceResponse: GeofenceResponse): List<Geofence> {
        val geofences = geofenceResponse.geofenceGroups
                .flatMap { it.geofences }
                .sortedBy {
                    (Location(LocationManager.GPS_PROVIDER).apply {
                        this.longitude = it.lon
                        this.latitude = it.lat
                    }.distanceTo(currentLocation) - it.radius)
                }
        if (limit > geofences.size) {
            return geofences
        }
        return geofences.subList(0, limit)
    }
}
