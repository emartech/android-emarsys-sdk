package com.emarsys.geofence

import com.emarsys.core.Mapper
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class GeofenceResponseMapper : Mapper<ResponseModel, GeofenceResponse> {

    override fun map(responseModel: ResponseModel?): GeofenceResponse {
        val geofenceGroups = mutableListOf<GeofenceGroup>()
        var refreshRadiusRatio: Double = GeofenceResponse.DEFAULT_REFRESH_RADIUS_RATIO
        if (responseModel != null) {
            try {
                val jsonResponse = JSONObject(responseModel.body)
                refreshRadiusRatio = jsonResponse.optDouble("refreshRadiusRatio", GeofenceResponse.DEFAULT_REFRESH_RADIUS_RATIO)
                val groupJsonArray = jsonResponse.getJSONArray("groups")

                geofenceGroups.addAll(extractGroupsFromJsonArray(groupJsonArray))
            } catch (exception: Exception) {
                when (exception) {
                    is JSONException -> {
                    }
                    else -> Logger.error(CrashLog(exception))
                }
            }
        }
        return GeofenceResponse(geofenceGroups, refreshRadiusRatio)
    }

    private fun extractGroupsFromJsonArray(groupJsonArray: JSONArray): List<GeofenceGroup> {
        val geofenceGroups = mutableListOf<GeofenceGroup>()
        for (i in 0 until groupJsonArray.length()) {
            val groupJson = groupJsonArray.getJSONObject(i)
            val groupId = groupJson.getString("id")
            val groupWaitInterval = groupJson.getDouble("waitInterval")
            val geofenceJsonArray = groupJson.getJSONArray("geofences")

            val geofences = extractGeofencesFromJsonArray(geofenceJsonArray)
            if (geofences.isEmpty()) {
                continue
            }
            val group = GeofenceGroup(groupId, groupWaitInterval, geofences)
            geofenceGroups.add(group)
        }
        return geofenceGroups
    }

    private fun extractGeofencesFromJsonArray(geofenceJsonArray: JSONArray): List<Geofence> {
        val geofences = mutableListOf<Geofence>()
        for (i in 0 until geofenceJsonArray.length()) {
            val geofenceJson = geofenceJsonArray.getJSONObject(i)

            val geofenceId = geofenceJson.getString("id")
            val lat = geofenceJson.getDouble("lat")
            val lon = geofenceJson.getDouble("lon")
            val r = geofenceJson.getInt("r")
            val waitInterval = geofenceJson.optDouble("waitInterval", 0.0)

            val triggerJsonArray = geofenceJson.getJSONArray("triggers")
            val triggers = extractTriggersFromJson(triggerJsonArray)

            val geofence = Geofence(geofenceId, lat, lon, r, waitInterval, triggers)
            geofences.add(geofence)
        }
        return geofences
    }

    fun extractTriggersFromJson(triggerJsonArray: JSONArray): List<Trigger> {
        val triggers = mutableListOf<Trigger>()

        for (i in 0 until triggerJsonArray.length()) {
            try {
                val triggerJson = triggerJsonArray.getJSONObject(i)
                val triggerId = triggerJson.getString("id")
                val type = TriggerType.valueOf(triggerJson.getString("type"))
                val loiteringDelay = triggerJson.optInt("loiteringDelay")
                if (!triggerJson.has("loiteringDelay") && type == TriggerType.DWELLING) {
                    continue
                }
                val action = triggerJson.getJSONObject("action")

                val trigger = Trigger(triggerId, type, loiteringDelay, action)

                triggers.add(trigger)
            } catch (exception: Exception) {
                when (exception) {
                    is IllegalArgumentException,
                    is JSONException -> {
                    }
                    else -> throw exception
                }
            }
        }
        return triggers
    }
}