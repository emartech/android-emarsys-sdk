package com.emarsys.mobileengage.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.emarsys.core.Mockable
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.validate.JsonObjectValidator
import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.SilentNotificationInformationCommand
import org.json.JSONException
import org.json.JSONObject

@Mockable
object MessagingServiceUtils {
    const val MESSAGE_FILTER = "ems_msg"

    @JvmStatic
    fun handleMessage(
        context: Context,
        remoteMessageData: Map<String, String>,
        deviceInfo: DeviceInfo,
        fileDownloader: FileDownloader,
        actionCommandFactory: ActionCommandFactory,
        remoteMessageMapper: RemoteMessageMapper
    ): Boolean {

        var handled = false
        if (isMobileEngageMessage(remoteMessageData)) {
            if (isSilent(remoteMessageData)) {
                createSilentPushCommands(actionCommandFactory, remoteMessageData).forEach {
                    mobileEngage().concurrentHandlerHolder.postOnMain {
                        it?.run()
                    }
                }
            } else {
                val notificationData = remoteMessageMapper.map(remoteMessageData)
                val notificationManager =
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                val collapseId = notificationData.notificationMethod.collapseId
                val notification = createNotification(
                    collapseId,
                    context.applicationContext,
                    remoteMessageData,
                    deviceInfo,
                    fileDownloader,
                    notificationData
                )
                when (notificationData.notificationMethod.operation) {
                    NotificationOperation.INIT, NotificationOperation.UPDATE -> {
                        notificationManager.notify(collapseId, notification)
                    }
                    NotificationOperation.DELETE -> {
                        notificationManager.cancel(collapseId)
                    }
                }
            }
            handled = true
        }
        return handled
    }

    fun createSilentPushCommands(
        actionCommandFactory: ActionCommandFactory,
        remoteMessageData: Map<String, String?>
    ): List<Runnable?> {
        val actionsJsonArray = JSONObject(remoteMessageData["ems"]).optJSONArray("actions")
        val actions: MutableList<Runnable?> = mutableListOf()
        val campaignId = JSONObject(remoteMessageData["ems"]).optString("multichannelId")
        if (campaignId.isNotEmpty()) {
            val silentNotificationInformationListenerProvider =
                mobileEngage().silentNotificationInformationListenerProvider
            actions.add(
                SilentNotificationInformationCommand(
                    silentNotificationInformationListenerProvider,
                    NotificationInformation(campaignId)
                )
            )
        }
        if (actionsJsonArray != null) {
            for (i in 0 until actionsJsonArray.length()) {
                val action = actionsJsonArray.optJSONObject(i)
                actions.add(actionCommandFactory.createActionCommand(action))
            }
        }
        return actions
    }

    fun isSilent(remoteMessageData: Map<String, String?>?): Boolean {
        val emsPayload = remoteMessageData?.get("ems")
        if (emsPayload != null) {
            return JSONObject(emsPayload).optBoolean("silent", false)
        }
        return false
    }

    @JvmStatic
    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>): Boolean {
        return remoteMessageData.isNotEmpty() && remoteMessageData.containsKey(MESSAGE_FILTER)
    }

    fun createNotification(
        notificationId: Int,
        context: Context,
        remoteMessageData: Map<String, String>,
        deviceInfo: DeviceInfo,
        fileDownloader: FileDownloader,
        notificationData: NotificationData
    ): Notification {
        val notifData = handleChannelIdMismatch(deviceInfo, notificationData, context)

        return createNotificationBuilder(notifData, context)
            .setupBuilder(
                context,
                remoteMessageData,
                notificationId,
                fileDownloader,
                notifData
            )
            .styleNotification(notifData)
            .build()
    }

    private fun createNotificationBuilder(
        notificationData: NotificationData,
        context: Context
    ): NotificationCompat.Builder {
        return if (notificationData.channelId == null) {
            NotificationCompat.Builder(context)
        } else {
            NotificationCompat.Builder(context, notificationData.channelId)
        }
    }

    private fun handleChannelIdMismatch(
        deviceInfo: DeviceInfo,
        notificationData: NotificationData,
        context: Context
    ): NotificationData {
        return if (AndroidVersionUtils.isOreoOrAbove && deviceInfo.isDebugMode && !isValidChannel(
                deviceInfo.notificationSettings,
                notificationData.channelId
            )
        ) {
            notificationData.copy(
                body = "DEBUG - channel_id mismatch: ${notificationData.channelId} not found!",
                channelId = createDebugChannel(context),
                title = "Emarsys SDK"
            )
        } else {
            notificationData
        }
    }

    private fun NotificationCompat.Builder.setupBuilder(
        context: Context,
        remoteMessageData: Map<String, String>,
        notificationId: Int,
        fileDownloader: FileDownloader,
        notificationData: NotificationData
    ): NotificationCompat.Builder {
        val actions =
            NotificationActionUtils.createActions(context, remoteMessageData, notificationId)
        val preloadedRemoteMessageData = createPreloadedRemoteMessageData(
            remoteMessageData,
            getInAppDescriptor(fileDownloader, remoteMessageData)
        )
        val resultPendingIntent = IntentUtils.createNotificationHandlerServicePendingIntent(
            context,
            preloadedRemoteMessageData,
            notificationId
        )

        this
            .setContentTitle(notificationData.title)
            .setContentText(notificationData.body)
            .setSmallIcon(notificationData.smallIconResourceId)
            .setAutoCancel(false)
            .setContentIntent(resultPendingIntent)
        for (i in actions.indices) {
            this.addAction(actions[i])
        }
        if (notificationData.colorResourceId != 0) {
            this.color = ContextCompat.getColor(context, notificationData.colorResourceId)
        }
        return this
    }

    fun NotificationCompat.Builder.styleNotification(notificationData: NotificationData): NotificationCompat.Builder {
        return when (notificationData.style) {
            "MESSAGE" -> MessageStyle.apply(this, notificationData)
            "THUMBNAIL" -> ThumbnailStyle.apply(this, notificationData)
            "BIG_PICTURE" -> BigPictureStyle.apply(this, notificationData)
            "BIG_TEXT" -> BigTextStyle.apply(this, notificationData)
            else -> DefaultStyle.apply(this, notificationData)
        }
    }

    fun getInAppDescriptor(
        fileDownloader: FileDownloader,
        remoteMessageData: Map<String, String?>?
    ): String? {
        var result: String? = null
        try {
            if (remoteMessageData != null) {
                val emsPayload = remoteMessageData["ems"]
                if (emsPayload != null) {
                    val inAppPayload = JSONObject(emsPayload).getJSONObject("inapp")
                    val errors = JsonObjectValidator
                        .from(inAppPayload)
                        .hasFieldWithType("campaign_id", String::class.java)
                        .hasFieldWithType("url", String::class.java)
                        .validate()
                    if (errors.isEmpty()) {
                        val inAppUrl: String = inAppPayload.getString("url")
                        val inAppDescriptor = JSONObject()
                        inAppDescriptor.put("campaignId", inAppPayload.getString("campaign_id"))
                        inAppDescriptor.put("url", inAppUrl)
                        inAppDescriptor.put("fileUrl", fileDownloader.download(inAppUrl))
                        result = inAppDescriptor.toString()
                    }
                }
            }
        } catch (ignored: JSONException) {
        }
        return result
    }

    fun createPreloadedRemoteMessageData(
        remoteMessageData: Map<String, String?>,
        inAppDescriptor: String?
    ): Map<String, String?> {
        val preloadedRemoteMessageData = mutableMapOf<String, String?>()
        val keys = remoteMessageData.keys
        for (key in keys) {
            preloadedRemoteMessageData[key] = remoteMessageData[key]
        }
        if (inAppDescriptor != null) {
            try {
                val ems = JSONObject(preloadedRemoteMessageData["ems"])
                ems.put("inapp", inAppDescriptor)
                preloadedRemoteMessageData["ems"] = ems.toString()
            } catch (ignored: JSONException) {
            }
        }
        return preloadedRemoteMessageData
    }

    private fun isValidChannel(
        notificationSettings: NotificationSettings,
        channelId: String?
    ): Boolean {
        return notificationSettings.channelSettings.any { it.channelId == channelId }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createDebugChannel(context: Context): String {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            "ems_debug",
            "Emarsys SDK Debug Messages",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)
        return notificationChannel.id
    }
}