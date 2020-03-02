package com.emarsys.mobileengage.geofence.model

data class GeofenceResponse(val geofenceGroups: List<GeofenceGroup>, val refreshRadiusRatio: Double = DEFAULT_REFRESH_RADIUS_RATIO) {
    companion object {
        const val DEFAULT_REFRESH_RADIUS_RATIO = 0.5
    }
}
