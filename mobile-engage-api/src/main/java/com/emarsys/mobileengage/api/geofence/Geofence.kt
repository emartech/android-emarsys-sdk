package com.emarsys.mobileengage.api.geofence

data class Geofence(val id: String,
                    val lat: Double,
                    val lon: Double,
                    val radius: Double,
                    val waitInterval: Double?,
                    val triggers: List<Trigger>)