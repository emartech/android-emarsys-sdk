package com.emarsys.mobileengage.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.Trigger
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.util.AndroidVersionUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import org.json.JSONObject
import com.emarsys.mobileengage.geofence.model.Geofence as MEGeofence

@Mockable
class DefaultGeofenceInternal(private val requestModelFactory: MobileEngageRequestModelFactory,
                              private val requestManager: RequestManager,
                              private val geofenceResponseMapper: GeofenceResponseMapper,
                              private val permissionChecker: PermissionChecker,
                              private val locationManager: LocationManager?,
                              private val geofenceFilter: GeofenceFilter,
                              private val geofencingClient: GeofencingClient,
                              private val context: Context,
                              private val actionCommandFactory: ActionCommandFactory) : GeofenceInternal {
    private val geofenceBroadcastReceiver = GeofenceBroadcastReceiver()
    private var geofenceResponse: GeofenceResponse? = null
    private lateinit var nearestGeofences: MutableList<MEGeofence>
    private var currentLocation: Location? = null
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent("com.emarsys.sdk.GEOFENCE_ACTION")
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private var receiverRegistered = false

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
            registerNearestGeofences(completionListener)
            if (!receiverRegistered) {
                context.registerReceiver(geofenceBroadcastReceiver, IntentFilter("com.emarsys.sdk.GEOFENCE_ACTION"))
                receiverRegistered = true
            }
        } else {
            val permissionName = findMissingPermission(fineLocationPermissionGranted, backgroundLocationPermissionGranted)
            completionListener?.onCompleted(MissingPermissionException("Couldn't acquire permission for $permissionName"))
        }
    }

    override fun disable() {
        context.unregisterReceiver(geofenceBroadcastReceiver)
        receiverRegistered = false
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    private fun registerNearestGeofences(completionListener: CompletionListener?) {
        currentLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        completionListener?.onCompleted(null)
        if (currentLocation != null && geofenceResponse != null) {
            nearestGeofences = geofenceFilter.findNearestGeofences(currentLocation!!, geofenceResponse!!).toMutableList()
            nearestGeofences.add(createRefreshAreaGeofence(nearestGeofences))
            registerGeofences(nearestGeofences)
        }
    }

    private fun findMissingPermission(fineLocationPermissionGranted: Boolean, backgroundLocationPermissionGranted: Boolean): String {
        return if (!fineLocationPermissionGranted && backgroundLocationPermissionGranted) {
            "ACCESS_FINE_LOCATION"
        } else if (!backgroundLocationPermissionGranted && fineLocationPermissionGranted) {
            "ACCESS_BACKGROUND_LOCATION"
        } else {
            "ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION"
        }
    }

    private fun createRefreshAreaGeofence(nearestGeofences: List<MEGeofence>): MEGeofence {
        val furthestGeofence = nearestGeofences.last()
        val result = floatArrayOf(1F)
        Location.distanceBetween(currentLocation!!.latitude, currentLocation!!.longitude, furthestGeofence.lat, furthestGeofence.lon, result)
        val radius = (result[0] - furthestGeofence.radius) * geofenceResponse!!.refreshRadiusRatio
        return MEGeofence("refreshArea", currentLocation!!.latitude, currentLocation!!.longitude, radius, null, listOf<Trigger>(Trigger("refreshAreaTriggerId", TriggerType.EXIT, 0, JSONObject())))
    }

    override fun registerGeofences(geofences: List<MEGeofence>) {
        val geofencesToRegister = geofences.map {
            Geofence.Builder()
                    .setRequestId(it.id)
                    .setCircularRegion(it.lon, it.lat, it.radius.toFloat())
                    .setExpirationDuration(Long.MAX_VALUE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
        }
        val geofenceRequest = GeofencingRequest.Builder().addGeofences(geofencesToRegister).build()
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
    }

    override fun onGeofenceTriggered(events: List<TriggeringGeofence>) {
        events.flatMap { triggeringGeofence ->
            nearestGeofences.filter { it.id == triggeringGeofence.geofenceId }
        }.flatMap { nearestTriggeredGeofences ->
            createActionsFromTriggers(nearestTriggeredGeofences)
        }.forEach { action ->
            Handler(Looper.getMainLooper()).post {
                action?.run()
            }
        }
        val refreshAreaTriggeringGeofence = events.find { it.geofenceId == "refreshArea" }
        if (refreshAreaTriggeringGeofence != null) {
            val fineLocationPermissionGranted = permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            val backgroundLocationPermissionGranted = if (AndroidVersionUtils.isBelowQ()) true else {
                permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            }

            if (fineLocationPermissionGranted && backgroundLocationPermissionGranted) {
                registerNearestGeofences(null)
            }
        }
    }

    private fun createActionsFromTriggers(it: MEGeofence): List<Runnable?> {
        return it.triggers.mapNotNull { actionCommandFactory.createActionCommand(it.action) }
    }
}