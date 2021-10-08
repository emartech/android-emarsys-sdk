package com.emarsys.mobileengage.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import androidx.annotation.RequiresPermission
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.MissingPermissionException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.geofence.Trigger
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.mobileengage.geofence.model.TriggeringEmarsysGeofence
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.google.android.gms.location.*
import org.json.JSONObject
import kotlin.math.abs
import com.emarsys.mobileengage.api.geofence.Geofence as MEGeofence

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
                              private val geofencePendingIntentProvider: GeofencePendingIntentProvider,
                              coreSdkHandler: CoreSdkHandler,
                              private val uiHandler: Handler,
                              private val initialEnterTriggerEnabledStorage: Storage<Boolean?>
) : GeofenceInternal {
    private companion object {
        const val FASTEST_INTERNAL: Long = 15_000
        const val INTERVAL: Long = 30_000
        const val MAX_WAIT_TIME: Long = 60_000
    }

    private val geofenceBroadcastReceiver = GeofenceBroadcastReceiver(coreSdkHandler)
    private var geofenceResponse: GeofenceResponse? = null
    private var nearestGeofences: MutableList<MEGeofence> = mutableListOf()
    override val registeredGeofences: List<MEGeofence>
        get() {
            return nearestGeofences
        }
    private var currentLocation: Location? = null
    private val geofencePendingIntent: PendingIntent by lazy {
        geofencePendingIntentProvider.providePendingIntent()
    }
    private var receiverRegistered = false
    private var initialEnterTriggerEnabled = initialEnterTriggerEnabledStorage.get() ?: false
    private var initialDwellingTriggerEnabled = false
    private var initialExitTriggerEnabled = false

    override fun fetchGeofences(completionListener: CompletionListener?) {
        if (!geofenceEnabledStorage.get()) {
            return
        }
        val requestModel = requestModelFactory.createFetchGeofenceRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                geofenceResponse = geofenceResponseMapper.map(responseModel)
                enable(completionListener)
            }

            override fun onError(id: String, responseModel: ResponseModel) {
            }

            override fun onError(id: String, cause: Exception) {
            }

        })
    }

    override fun enable(completionListener: CompletionListener?) {
        val missingPermissions = findMissingPermissions()

        if (missingPermissions == null) {
            if (!geofenceEnabledStorage.get()) {
                geofenceEnabledStorage.set(true)

                sendStatusLog(
                        mapOf("completionListener" to (completionListener != null)),
                        mapOf("geofenceEnabled" to true)
                )

                if (geofenceResponse == null) {
                    fetchGeofences(completionListener)
                    return
                }
            }
            registerNearestGeofences(completionListener)
            registerBroadcastReceiver()
        } else {
            completionListener?.onCompleted(MissingPermissionException("Couldn't acquire permission for $missingPermissions"))
        }
    }

    override fun disable() {
        context.unregisterReceiver(geofenceBroadcastReceiver)
        fusedLocationProviderClient.removeLocationUpdates(geofencePendingIntent)
        geofenceEnabledStorage.set(false)
        receiverRegistered = false

        sendStatusLog(statusMap = mapOf(
                "geofenceEnabled" to false
        ))
    }

    override fun isEnabled(): Boolean {
        return geofenceEnabledStorage.get()
    }

    private fun registerBroadcastReceiver() {
        if (!receiverRegistered) {
            uiHandler.post {
                context.registerReceiver(geofenceBroadcastReceiver, IntentFilter("com.emarsys.sdk.GEOFENCE_ACTION"))
            }
            receiverRegistered = true
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    private fun validateBackgroundPermission() {
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun registerNearestGeofences(completionListener: CompletionListener?) {
        if (!AndroidVersionUtils.isBelowQ()) {
            validateBackgroundPermission()
        }
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener { currentLocation = it }

        fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    fastestInterval = FASTEST_INTERNAL
                    interval = INTERVAL
                    maxWaitTime = MAX_WAIT_TIME
                    priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                },
                geofencePendingIntent)

        completionListener?.onCompleted(null)

        if (currentLocation != null && geofenceResponse != null) {
            nearestGeofences = geofenceFilter.findNearestGeofences(currentLocation!!, geofenceResponse!!).toMutableList()
            nearestGeofences.add(createRefreshAreaGeofence(nearestGeofences))
            registerGeofences(nearestGeofences)
        }
    }

    private fun findMissingPermissions(): String? {
        val locationPermissionGranted = permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val backgroundLocationPermissionGranted = if (AndroidVersionUtils.isBelowQ()) true else {
            permissionChecker.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        return if (locationPermissionGranted && backgroundLocationPermissionGranted) {
            null
        } else {
            return missingPermissionName(locationPermissionGranted, backgroundLocationPermissionGranted)
        }
    }

    private fun missingPermissionName(locationPermissionGranted: Boolean, backgroundLocationPermissionGranted: Boolean): String {
        return if (!locationPermissionGranted && backgroundLocationPermissionGranted) {
            "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION"
        } else if (!backgroundLocationPermissionGranted && locationPermissionGranted) {
            "ACCESS_BACKGROUND_LOCATION"
        } else {
            "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION and ACCESS_BACKGROUND_LOCATION"
        }
    }

    private fun createRefreshAreaGeofence(nearestGeofences: List<MEGeofence>): MEGeofence {
        val furthestGeofence = nearestGeofences.last()
        val result = floatArrayOf(1F)
        Location.distanceBetween(currentLocation!!.latitude, currentLocation!!.longitude, furthestGeofence.lat, furthestGeofence.lon, result)
        val radius = abs((result[0] - furthestGeofence.radius) * geofenceResponse!!.refreshRadiusRatio)
        return MEGeofence("refreshArea", currentLocation!!.latitude, currentLocation!!.longitude, radius, null,
                listOf(Trigger("refreshAreaTriggerId", TriggerType.EXIT, 0, JSONObject())))
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
        val geofenceRequest = GeofencingRequest.Builder()
                .addGeofences(geofencesToRegister)
                .setInitialTrigger(calcInitialTrigger())
                .build()
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)

        sendStatusLog(statusMap = mapOf(
                "registeredGeofences" to geofencesToRegister.size
        ))
    }

    private fun calcInitialTrigger(): Int {
        var result = 0
        if (initialEnterTriggerEnabled) result += GeofencingRequest.INITIAL_TRIGGER_ENTER
        if (initialDwellingTriggerEnabled) result += GeofencingRequest.INITIAL_TRIGGER_DWELL
        if (initialExitTriggerEnabled) result += GeofencingRequest.INITIAL_TRIGGER_EXIT
        return result
    }

    override fun onGeofenceTriggered(triggeringEmarsysGeofences: List<TriggeringEmarsysGeofence>) {
        extractActions(triggeringEmarsysGeofences).run {
            executeActions(this)
        }

        reRegisterNearestGeofences(triggeringEmarsysGeofences)
    }

    private fun reRegisterNearestGeofences(triggeringEmarsysGeofences: List<TriggeringEmarsysGeofence>) {
        val refreshAreaGeofence = triggeringEmarsysGeofences.find { it.geofenceId == "refreshArea" && it.triggerType == TriggerType.EXIT }
        if (refreshAreaGeofence != null && findMissingPermissions() == null) {
            registerNearestGeofences(null)
        }
    }

    private fun extractActions(triggeringGeofences: List<TriggeringEmarsysGeofence>): List<Runnable?> {
        val nearestTriggeredGeofences = triggeringGeofences
                .flatMap { triggeringGeofence ->
                    collectTriggeredGeofencesWithTriggerType(triggeringGeofence)
                }
        return nearestTriggeredGeofences
                .flatMap { nearestTriggeredGeofencesWithTrigger ->
                    createActionsFromTriggers(nearestTriggeredGeofencesWithTrigger.first, nearestTriggeredGeofencesWithTrigger.second)
                }
    }

    private fun executeActions(actions: List<Runnable?>) {
        actions.forEach { action ->
            uiHandler.post {
                action?.run()
            }
        }
    }

    private fun collectTriggeredGeofencesWithTriggerType(triggeringGeofence: TriggeringEmarsysGeofence): List<Pair<MEGeofence, TriggerType>> {
        return nearestGeofences
                .filter {
                    it.id == triggeringGeofence.geofenceId
                            && it.triggers.any { trigger -> trigger.type == triggeringGeofence.triggerType }
                }.map { geofence -> Pair(geofence, triggeringGeofence.triggerType) }
    }

    override fun setEventHandler(eventHandler: EventHandler) {
        this.geofenceEventHandlerProvider.eventHandler = eventHandler
    }

    override fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        this.initialEnterTriggerEnabled = enabled
        initialEnterTriggerEnabledStorage.set(enabled)
    }

    private fun createActionsFromTriggers(geofence: MEGeofence, triggerType: TriggerType): List<Runnable?> {
        return geofence.triggers.filter { trigger ->
            trigger.type == triggerType
        }.mapNotNull { actionCommandFactory.createActionCommand(it.action) }
    }

    private fun sendStatusLog(parameters: Map<String, Any?>? = mapOf(), statusMap: Map<String, Any>? = mapOf()) {
        Logger.debug(StatusLog(DefaultGeofenceInternal::class.java, SystemUtils.getCallerMethodName(), parameters, statusMap))
    }
}