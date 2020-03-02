package com.emarsys.mobileengage.geofence.model

data class GeofenceGroup(val id: String,
                         val waitInterval: Double?,
                         val geofences: List<Geofence>)