package com.emarsys.mobileengage.geofence

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory

class DefaultGeofenceInternal(private val requestModelFactory: MobileEngageRequestModelFactory,
                              private val requestManager: RequestManager,
                              private val geofenceResponseMapper: GeofenceResponseMapper,
                              private val permissionChecker: PermissionChecker,
                              private val locationManager: LocationManager,
                              private val geofenceFiler: GeofenceFilter) : GeofenceInternal {
    private var geofences: GeofenceResponse? = null
    private var currentLocation: Location? = null

    override fun fetchGeofences() {
        val requestModel = requestModelFactory.createFetchGeofenceRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String?, responseModel: ResponseModel?) {
                if (responseModel != null) {
                    geofences = geofenceResponseMapper.map(responseModel)
                }
            }

            override fun onError(id: String?, responseModel: ResponseModel?) {
            }

            override fun onError(id: String?, cause: Exception?) {
            }

        })
    }

    override fun enable(completionListener: CompletionListener?) {
        val locationPermissionGranted = permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (locationPermissionGranted) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            completionListener?.onCompleted(null)
            if (currentLocation != null && geofences != null) {
                geofenceFiler.findNearestGeofences(currentLocation!!, geofences!!)
            }
        } else {
            completionListener?.onCompleted(MissingPermissionException("Couldn't acquire permission for ACCESS_FINE_LOCATION"))
        }
    }
}