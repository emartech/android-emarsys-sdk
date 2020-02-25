package com.emarsys.geofence

data class GeofenceGroup(val id: String,
                         val waitInterval: Double?,
                         val geofences: List<Geofence>)