package com.emarsys.mobileengage.geofence

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.Trigger
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.util.AndroidVersionUtils
import org.json.JSONObject

@Mockable
class DefaultGeofenceInternal(private val requestModelFactory: MobileEngageRequestModelFactory,
                              private val requestManager: RequestManager,
                              private val geofenceResponseMapper: GeofenceResponseMapper,
                              private val permissionChecker: PermissionChecker,
                              private val locationManager: LocationManager?,
                              private val geofenceFiler: GeofenceFilter) : GeofenceInternal {
    private var geofenceResponse: GeofenceResponse? = null
    private var currentLocation: Location? = null

    override fun fetchGeofences() {
        val requestModel = requestModelFactory.createFetchGeofenceRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String?, responseModel: ResponseModel?) {
                if (responseModel != null) {
                    geofenceResponse = geofenceResponseMapper.map(responseModel)
                }
            }

            override fun onError(id: String?, responseModel: ResponseModel?) {
            }

            override fun onError(id: String?, cause: Exception?) {
            }

        })
    }

    override fun enable(completionListener: CompletionListener?) {
        val fineLocationPermissionGranted = permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val backgroundLocationPermissionGranted = if (AndroidVersionUtils.isBelowQ()) true else {
            permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        if (fineLocationPermissionGranted && backgroundLocationPermissionGranted) {
            currentLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            completionListener?.onCompleted(null)
            if (currentLocation != null && geofenceResponse != null) {
                val nearestGeofences = geofenceFiler.findNearestGeofences(currentLocation!!, geofenceResponse!!).toMutableList()
                nearestGeofences.add(createRefreshAreaGeofence(nearestGeofences))
                registerGeofences(nearestGeofences)
            }
        } else {
            val permissionName = if (!fineLocationPermissionGranted && backgroundLocationPermissionGranted) {
                "ACCESS_FINE_LOCATION"
            } else if (!backgroundLocationPermissionGranted && fineLocationPermissionGranted) {
                "ACCESS_BACKGROUND_LOCATION"
            } else {
                "ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION"
            }
            completionListener?.onCompleted(MissingPermissionException("Couldn't acquire permission for $permissionName"))
        }
    }

    private fun createRefreshAreaGeofence(nearestGeofences: List<Geofence>): Geofence {
        val furthestGeofence = nearestGeofences.last()
        val result = floatArrayOf(1F)
        Location.distanceBetween(currentLocation!!.latitude, currentLocation!!.longitude, furthestGeofence.lat, furthestGeofence.lon, result)
        val radius = (result[0] - furthestGeofence.radius) * geofenceResponse!!.refreshRadiusRatio
        return Geofence("refreshArea", currentLocation!!.latitude, currentLocation!!.longitude, radius, null, listOf<Trigger>(Trigger("refreshAreaTriggerId", TriggerType.EXIT, 0, JSONObject())))
    }

    override fun registerGeofences(geofences: List<Geofence>) {

    }
}