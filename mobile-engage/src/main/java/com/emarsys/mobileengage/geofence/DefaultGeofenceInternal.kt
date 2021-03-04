package com.emarsys.mobileengage.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.Trigger
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.google.android.gms.location.*
import org.json.JSONObject
import com.emarsys.mobileengage.geofence.model.Geofence as MEGeofence

@Mockable
class DefaultGeofenceInternal(private val requestModelFactory: MobileEngageRequestModelFactory,
                              private val requestManager: RequestManager,
                              private val geofenceResponseMapper: GeofenceResponseMapper,
                              private val permissionChecker: PermissionChecker,
                              private val fusedLocationProviderClient: FusedLocationProviderClient,
                              private val geofenceFilter: GeofenceFilter,
                              private val geofencingClient: GeofencingClient,
                              private val context: Context,
                              private val actionCommandFactory: ActionCommandFactory,
                              private val geofenceEventHandlerProvider: EventHandlerProvider,
                              private val geofenceEnabledStorage: Storage<Boolean>,
                              private val geofencePendingIntentProvider: GeofencePendingIntentProvider
) : GeofenceInternal, LocationListener {
    private companion object {
        const val FASTEST_INTERNAL: Long = 15_000
        const val INTERVAL: Long = 30_000
        const val MAX_WAIT_TIME: Long = 60_000
    }

    private val geofenceBroadcastReceiver = GeofenceBroadcastReceiver(getDependency())
    private var geofenceResponse: GeofenceResponse? = null
    private var nearestGeofences: MutableList<MEGeofence> = mutableListOf()
    private var currentLocation: Location? = null
    private val geofencePendingIntent: PendingIntent by lazy {
        geofencePendingIntentProvider.providePendingIntent()
    }
    private var receiverRegistered = false

    override fun fetchGeofences(completionListener: CompletionListener?) {
        if (!geofenceEnabledStorage.get()) {
            return
        }
        val requestModel = requestModelFactory.createFetchGeofenceRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String?, responseModel: ResponseModel?) {
                if (responseModel != null) {
                    geofenceResponse = geofenceResponseMapper.map(responseModel)
                    enable(completionListener)
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
            if (!geofenceEnabledStorage.get()) {
                geofenceEnabledStorage.set(true)

                Logger.debug(StatusLog(DefaultGeofenceInternal::class.java,
                        SystemUtils.getCallerMethodName(),
                        mapOf("completionListener" to (completionListener != null)),
                        mapOf("geofenceEnabled" to true)))

                if (geofenceResponse == null) {
                    fetchGeofences(completionListener)
                    return
                }
            }
            registerNearestGeofences(completionListener)
            if (!receiverRegistered) {
                Handler(Looper.getMainLooper()).post {
                    context.registerReceiver(geofenceBroadcastReceiver, IntentFilter("com.emarsys.sdk.GEOFENCE_ACTION"))
                }
                receiverRegistered = true
            }
        } else {
            val permissionName = findMissingPermission(fineLocationPermissionGranted, backgroundLocationPermissionGranted)
            completionListener?.onCompleted(MissingPermissionException("Couldn't acquire permission for $permissionName"))
        }
    }

    override fun disable() {
        context.unregisterReceiver(geofenceBroadcastReceiver)
        fusedLocationProviderClient.removeLocationUpdates(geofencePendingIntent)
        geofenceEnabledStorage.set(false)
        receiverRegistered = false

        Logger.debug(StatusLog(DefaultGeofenceInternal::class.java, SystemUtils.getCallerMethodName(), mapOf(), mapOf(
                "geofenceEnabled" to false
        )))
    }

    override fun isEnabled(): Boolean {
        return geofenceEnabledStorage.get()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    private fun registerNearestGeofences(completionListener: CompletionListener?) {
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener { currentLocation = it }

        fusedLocationProviderClient.requestLocationUpdates(LocationRequest()
                .setFastestInterval(FASTEST_INTERNAL)
                .setInterval(INTERVAL)
                .setMaxWaitTime(MAX_WAIT_TIME)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY), geofencePendingIntent)

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
        return MEGeofence("refreshArea", currentLocation!!.latitude, currentLocation!!.longitude, radius, null, listOf(Trigger("refreshAreaTriggerId", TriggerType.EXIT, 0, JSONObject())))
    }

    override fun registerGeofences(geofences: List<MEGeofence>) {
        val geofencesToRegister = geofences.map {
            Geofence.Builder()
                    .setRequestId(it.id)
                    .setCircularRegion(it.lat, it.lon, it.radius.toFloat())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
        }
        val geofenceRequest = GeofencingRequest.Builder().addGeofences(geofencesToRegister).build()
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)

        Logger.debug(StatusLog(DefaultGeofenceInternal::class.java, SystemUtils.getCallerMethodName(), mapOf(), mapOf(
                "registeredGeofences" to geofencesToRegister.size
        )))
    }

    override fun onGeofenceTriggered(events: List<TriggeringGeofence>) {
        events.flatMap { triggeringGeofence ->
            nearestGeofences.filter {
                it.id == triggeringGeofence.geofenceId && it.triggers.any { trigger -> trigger.type == triggeringGeofence.triggerType }
            }
        }.flatMap { nearestTriggeredGeofences ->
            createActionsFromTriggers(nearestTriggeredGeofences)
        }.forEach { action ->
            Handler(Looper.getMainLooper()).post {
                action?.run()
            }
        }
        val refreshAreaTriggeringGeofence = events.find { it.geofenceId == "refreshArea" && it.triggerType == TriggerType.EXIT }
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

    override fun setEventHandler(eventHandler: EventHandler) {
        this.geofenceEventHandlerProvider.eventHandler = eventHandler
    }

    private fun createActionsFromTriggers(it: MEGeofence): List<Runnable?> {
        return it.triggers.mapNotNull { actionCommandFactory.createActionCommand(it.action) }
    }

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}