package com.emarsys.mobileengage.geofence.model

data class Geofence(val id: String,
                    val lat: Double,
                    val lon: Double,
                    val r: Int,
                    val waitInterval: Double?,
                    val triggers: List<Trigger>)