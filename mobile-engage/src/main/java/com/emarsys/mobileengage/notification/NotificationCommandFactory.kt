package com.emarsys.mobileengage.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.notification.command.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Mockable
class NotificationCommandFactory(private val context: Context) {
    private companion object {
        const val SDK_ACTIVITY_NAME = "com.emarsys.NotificationOpenedActivity"
    }

    private val eventServiceInternal by lazy { mobileEngage().eventServiceInternal }
    private val pushInternal by lazy { mobileEngage().pushInternal }
    private val actionCommandFactory by lazy { mobileEngage().notificationActionCommandFactory }
    private val notificationInformationListenerProvider by lazy { mobileEngage().notificationInformationListenerProvider }

    fun createNotificationCommand(intent: Intent): Runnable {
        val actionId = intent.action
        val bundle = intent.getBundleExtra("payload")
        val action = getAction(bundle, actionId)

        val commands = createMandatoryCommands(intent, bundle)
        if (action == null || action.optString("type") != "Dismiss") {
            val currentActivity = mobileEngage().currentActivityProvider.get()
            if (currentActivity == null ||
                currentActivity.toString().startsWith(SDK_ACTIVITY_NAME)
            ) {
                commands.add(
                    LaunchApplicationCommand(
                        intent,
                        context,
                        LaunchActivityCommandLifecycleCallbacksFactory()
                    )
                )
            }
        }
        val inappCommand = handleInapp(intent, bundle)
        if (inappCommand != null) {
            commands.add(inappCommand)
        }
        val notificationInformationCommand = handlePushInformation(bundle)
        commands.add(notificationInformationCommand)
        val trackingCommand = handleTracking(intent, actionId, bundle, action)
        commands.add(trackingCommand)
        val actionCommand = handleAction(action)
        if (actionCommand != null) {
            commands.add(actionCommand)
        }
        return CompositeCommand(commands.filterNotNull())
    }

    private fun handlePushInformation(payload: Bundle?): NotificationInformationCommand? {
        if (payload != null) {
            val ems = payload.getString("ems")
            if (ems != null) {
                val emsJson = JSONObject(ems)
                val campaignId = emsJson.optString("multichannelId")
                if (campaignId.isNotEmpty()) {
                    return NotificationInformationCommand(
                        notificationInformationListenerProvider,
                        NotificationInformation(emsJson.getString("multichannelId"))
                    )
                }
            }
        }
        return null
    }

    private fun createMandatoryCommands(intent: Intent, bundle: Bundle?): MutableList<Runnable?> {
        val commands: MutableList<Runnable?> = ArrayList()
        commands.add(DismissNotificationCommand(context, intent))
        if (bundle != null) {
            commands.add(actionCommandFactory.createActionCommand(createActionCommandPayload(bundle)))
        }
        return commands
    }

    private fun handleAction(action: JSONObject?): Runnable? {
        var result: Runnable? = null
        if (action != null) {
            val actionCommand = actionCommandFactory.createActionCommand(action)
            if (actionCommand != null) {
                result = actionCommand
            }
        }
        return result
    }

    private fun handleTracking(
        intent: Intent,
        actionId: String?,
        bundle: Bundle?,
        action: JSONObject?
    ): Runnable {
        return if (action != null && actionId != null) {
            TrackActionClickCommand(eventServiceInternal, actionId, extractSid(bundle))
        } else {
            TrackMessageOpenCommand(pushInternal, intent)
        }
    }

    private fun handleInapp(intent: Intent, bundle: Bundle?): Runnable? {
        var result: Runnable? = null
        if (hasInapp(bundle)) {
            result = PreloadedInappHandlerCommand(intent)
        }
        return result
    }

    private fun hasInapp(payload: Bundle?): Boolean {
        var result = false
        try {
            if (payload != null) {
                val ems = payload.getString("ems")
                if (ems != null) {
                    val emsJson = JSONObject(ems)
                    JSONObject(emsJson.getString("inapp"))
                    result = true
                }
            }
        } catch (ignored: JSONException) {
        }
        return result
    }

    private fun getAction(bundle: Bundle?, actionId: String?): JSONObject? {
        var result: JSONObject? = null
        if (bundle != null) {
            val emsPayload = bundle.getString("ems")
            if (emsPayload != null) {
                try {
                    result = if (actionId != null) {
                        val actions = JSONObject(emsPayload).getJSONArray("actions")
                        actionCommandFactory.findActionWithId(actions, actionId)
                    } else {
                        JSONObject(emsPayload).getJSONObject("default_action")
                    }
                } catch (ignored: JSONException) {
                }
            }
        }
        return result
    }

    private fun extractSid(bundle: Bundle?): String {
        var sid: String? = null
        if (bundle != null && bundle.containsKey("u")) {
            try {
                sid = JSONObject(bundle.getString("u")!!).getString("sid")
            } catch (ignore: JSONException) {
            }
        }
        if (sid == null) {
            sid = "Missing sid"
        }
        return sid
    }

    private fun createActionCommandPayload(bundle: Bundle): JSONObject {
        val payloadMap: MutableMap<String?, Any?> = HashMap()
        payloadMap["type"] = "MEAppEvent"
        payloadMap["name"] = "push:payload"
        payloadMap["payload"] = extractMandatoryActionPayload(bundle)
        return JSONObject(payloadMap)
    }

    private fun extractMandatoryActionPayload(bundle: Bundle?): JSONObject {
        val json = JSONObject()
        if (bundle != null) {
            val keys = bundle.keySet()
            for (key in keys) {
                try {
                    json.put(key, JSONObject.wrap(bundle[key]))
                } catch (ignored: JSONException) {
                }
            }
        }
        return json
    }

}