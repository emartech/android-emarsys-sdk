package com.emarsys.mobileengage.geofence.model

import com.emarsys.mobileengage.api.geofence.Geofence

data class GeofenceGroup(val id: String,
                         val waitInterval: Double?,
                         val geofences: List<Geofence>)