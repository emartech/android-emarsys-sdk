package com.emarsys.mobileengage.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.emarsys.core.Mockable
import com.emarsys.core.util.JsonUtils
import com.emarsys.mobileengage.api.NotificationEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.mobileengage.notification.command.CustomEventCommand
import com.emarsys.mobileengage.notification.command.OpenExternalUrlCommand
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Mockable
class ActionCommandFactory(private val context: Context,
                           private val eventServiceInternal: EventServiceInternal,
                           private val notificationEventHandler: NotificationEventHandler?) {


    fun createActionCommand(action: JSONObject): Runnable? {
        var result: Runnable? = null
        val type: String
        try {
            type = action.getString("type")
            if ("MEAppEvent" == type) {
                result = createAppEventCommand(action)
            }
            if ("OpenExternalUrl" == type) {
                result = createOpenExternalUrlCommand(action)
            }
            if ("MECustomEvent" == type) {
                result = createCustomEventCommand(action)
            }
        } catch (ignored: JSONException) {
        }
        return result
    }

    @Throws(JSONException::class)
    fun findActionWithId(actions: JSONArray, actionId: String): JSONObject? {
        for (i in 0..actions.length()) {
            val action = actions.optJSONObject(i)
            if (action != null && actionId == action.optString("id")) {
                return action
            }
        }
        throw JSONException("Cannot find action with id: $actionId")
    }

    @Throws(JSONException::class)
    private fun createAppEventCommand(action: JSONObject): Runnable? {
        return AppEventCommand(
                context,
                notificationEventHandler,
                action.getString("name"),
                action.optJSONObject("payload"))
    }

    private fun createOpenExternalUrlCommand(action: JSONObject): Runnable? {
        var result: Runnable? = null
        val link = Uri.parse(action.getString("url"))
        val externalCommandIntent = Intent(Intent.ACTION_VIEW, link)
        externalCommandIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (externalCommandIntent.resolveActivity(context.packageManager) != null) {
            result = OpenExternalUrlCommand(externalCommandIntent, context)
        }
        return result
    }

    @Throws(JSONException::class)
    private fun createCustomEventCommand(action: JSONObject): Runnable? {
        val name = action.getString("name")
        val payload = action.optJSONObject("payload")
        var eventAttribute: Map<String?, String?>? = null
        if (payload != null) {
            eventAttribute = JsonUtils.toFlatMap(payload)
        }
        return CustomEventCommand(eventServiceInternal, name, eventAttribute)
    }
}