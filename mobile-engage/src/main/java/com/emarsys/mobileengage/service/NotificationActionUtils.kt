package com.emarsys.mobileengage.service

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emarsys.core.validate.JsonObjectValidator
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.notification.NotificationCommandFactory
import org.json.JSONException
import org.json.JSONObject

object NotificationActionUtils {
    @JvmStatic
    fun handleAction(intent: Intent, commandFactory: NotificationCommandFactory) {
        mobileEngage().concurrentHandlerHolder.coreHandler.post {
            val command = commandFactory.createNotificationCommand(intent)
            command.run()
        }

    }

    fun createActions(
        context: Context,
        remoteMessageData: Map<String, String>,
        notificationId: String
    ): List<NotificationCompat.Action> {
        val result: MutableList<NotificationCompat.Action> = ArrayList()
        val emsPayload = remoteMessageData["ems"]
        if (emsPayload != null) {
            try {
                val actions = JSONObject(emsPayload).getJSONArray("actions")
                for (i in 0 until actions.length()) {
                    val action = createAction(
                            actions.getJSONObject(i),
                            context,
                            remoteMessageData,
                            notificationId)
                    if (action != null) {
                        result.add(action)
                    }
                }
            } catch (ignored: JSONException) {
            }
        }
        return result
    }

    private fun createAction(
        action: JSONObject,
        context: Context,
        remoteMessageData: Map<String, String>,
        notificationId: String
    ): NotificationCompat.Action? {
        var result: NotificationCompat.Action? = null
        try {
            val actionId = action.getString("id")
            val validationErrors = validate(action)
            if (validationErrors.isEmpty()) {
                result = NotificationCompat.Action.Builder(
                        0,
                        action.getString("title"),
                        IntentUtils.createNotificationHandlerServicePendingIntent(context, remoteMessageData, notificationId, actionId)).build()
            }
        } catch (ignored: JSONException) {
        }
        return result
    }

    @Throws(JSONException::class)
    private fun validate(action: JSONObject): List<String> {
        val actionType = action.getString("type")
        val jsonObjectValidator = JsonObjectValidator.from(action)
        if ("MEAppEvent" == actionType) {
            jsonObjectValidator.hasField("name")
        }
        if ("OpenExternalUrl" == actionType) {
            jsonObjectValidator.hasField("url")
        }
        if ("MECustomEvent" == actionType) {
            jsonObjectValidator.hasField("name")
        }
        return jsonObjectValidator.validate()
    }
}