package com.emarsys.mobileengage.notification

import android.content.Context
import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.notification.command.CompositeCommand
import com.emarsys.mobileengage.notification.command.DismissNotificationCommand
import com.emarsys.mobileengage.notification.command.LaunchApplicationCommand
import com.emarsys.mobileengage.notification.command.NotificationInformationCommand
import com.emarsys.mobileengage.notification.command.PreloadedInappHandlerCommand
import com.emarsys.mobileengage.notification.command.TrackActionClickCommand
import com.emarsys.mobileengage.notification.command.TrackMessageOpenCommand
import com.emarsys.mobileengage.service.NotificationData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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
        val notificationData = if (AndroidVersionUtils.isBelowTiramisu) intent.getParcelableExtra("payload") else intent.getParcelableExtra("payload", NotificationData::class.java)
        val action = getAction(notificationData, actionId)

        val commands = createMandatoryCommands(notificationData)
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
        val inappCommand = handleInapp(notificationData)
        if (inappCommand != null) {
            commands.add(inappCommand)
        }
        val notificationInformationCommand = handlePushInformation(notificationData)
        commands.add(notificationInformationCommand)
        val trackingCommand = handleTracking(actionId, notificationData, action)
        commands.add(trackingCommand)
        val actionCommand = handleAction(action)
        if (actionCommand != null) {
            commands.add(actionCommand)
        }
        return CompositeCommand(commands.filterNotNull())
    }

    private fun handlePushInformation(notificationData: NotificationData?): NotificationInformationCommand? {
        val campaignId = notificationData?.campaignId
        if (!campaignId.isNullOrEmpty()) {
            return NotificationInformationCommand(
                notificationInformationListenerProvider,
                NotificationInformation(campaignId)
            )
        }

        return null
    }

    private fun createMandatoryCommands(
        notificationData: NotificationData?
    ): MutableList<Runnable?> {
        val commands: MutableList<Runnable?> = ArrayList()
        commands.add(DismissNotificationCommand(context, notificationData))
        if (notificationData != null) {
            commands.add(
                actionCommandFactory.createActionCommand(
                    createActionCommandPayload(
                        notificationData
                    )
                )
            )
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
        actionId: String?,
        notificationData: NotificationData?,
        action: JSONObject?
    ): Runnable {
        return if (action != null && actionId != null) {
            TrackActionClickCommand(eventServiceInternal, actionId, notificationData?.sid)
        } else {
            TrackMessageOpenCommand(pushInternal, notificationData?.sid)
        }
    }

    private fun handleInapp(notificationData: NotificationData?): Runnable? {
        var result: Runnable? = null
        if (notificationData?.inapp != null) {
            result = PreloadedInappHandlerCommand(notificationData)
        }
        return result
    }

    private fun getAction(notificationData: NotificationData?, actionId: String?): JSONObject? {
        var result: JSONObject? = null
        try {
            if (notificationData?.actions != null && actionId != null) {
                result = actionCommandFactory.findActionWithId(
                    JSONArray(notificationData.actions),
                    actionId
                )
            } else if (notificationData?.defaultAction != null) {
                result = JSONObject(notificationData.defaultAction)
            }
        } catch (ignored: JSONException) {
        }
        return result
    }

    private fun createActionCommandPayload(notificationData: NotificationData): JSONObject {
        val payloadMap: MutableMap<String?, Any?> = HashMap()
        payloadMap["type"] = "MEAppEvent"
        payloadMap["name"] = "push:payload"
        payloadMap["payload"] = extractMandatoryActionPayload(notificationData)
        return JSONObject(payloadMap)
    }

    private fun extractMandatoryActionPayload(notificationData: NotificationData): JSONObject {
        val actions = notificationData.actions?.let {
            JSONArray(it)
        }
        val json = JSONObject()
        json.put("image", notificationData.image)
        json.put("iconImage", notificationData.iconImage)
        json.put("style", notificationData.style)
        json.put("title", notificationData.title)
        json.put("body", notificationData.body)
        json.put("channelId", notificationData.channelId)
        json.put("campaignId", notificationData.campaignId)
        json.put("sid", notificationData.sid)
        json.put("smallIconResourceId", notificationData.smallIconResourceId)
        json.put("colorResourceId", notificationData.colorResourceId)
        json.put("notificationMethod", notificationData.notificationMethod)
        json.put("actions", actions)
        json.put("defaultAction", notificationData.defaultAction)
        json.put("inapp", notificationData.inapp)

        return json
    }

}