package com.emarsys.geofence

data class GeofenceResponse(val geofenceGroups: List<GeofenceGroup>, val refreshRadiusRatio: Double? = 0.5)
