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
import com.emarsys.core.util.ImageUtils
import com.emarsys.core.validate.JsonObjectValidator
import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.SilentNotificationInformationCommand
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperFactory
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Mockable
object MessagingServiceUtils {
    const val MESSAGE_FILTER = "ems_msg"
    const val V2_MESSAGE_FILTER = "ems.version"

    @JvmStatic
    fun handleMessage(
        context: Context,
        remoteMessageData: Map<String, String>,
        deviceInfo: DeviceInfo,
        fileDownloader: FileDownloader,
        actionCommandFactory: ActionCommandFactory,
        remoteMessageMapperFactory: RemoteMessageMapperFactory
    ) {
        val remoteMessageMapper = remoteMessageMapperFactory.create(remoteMessageData)
        val notificationData = remoteMessageMapper.map(remoteMessageData)
        if (isSilent(remoteMessageData)) {
            createSilentPushCommands(actionCommandFactory, notificationData).forEach {
                mobileEngage().concurrentHandlerHolder.postOnMain {
                    it?.run()
                }
            }
        } else {
            val notificationManager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            val collapseId = notificationData.collapseId
            val notification = createNotification(
                context.applicationContext,
                deviceInfo,
                fileDownloader,
                notificationData
            )
            when (NotificationOperation.valueOf(notificationData.operation)) {
                NotificationOperation.INIT, NotificationOperation.UPDATE -> {
                    notificationManager.notify(collapseId, collapseId.hashCode(), notification)
                }

                NotificationOperation.DELETE -> {
                    notificationManager.cancel(collapseId, collapseId.hashCode())
                }
            }
        }
    }

    fun createSilentPushCommands(
        actionCommandFactory: ActionCommandFactory,
        notificationData: NotificationData
    ): List<Runnable?> {
        val actionsJsonArray = notificationData.actions?.let { JSONArray(it) } ?: null
        val actions: MutableList<Runnable?> = mutableListOf()
        if (!notificationData.campaignId.isNullOrEmpty()) {
            val silentNotificationInformationListenerProvider =
                mobileEngage().silentNotificationInformationListenerProvider
            actions.add(
                SilentNotificationInformationCommand(
                    silentNotificationInformationListenerProvider,
                    NotificationInformation(notificationData.campaignId)
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
        val v1IsSilent = if (emsPayload != null) {
            JSONObject(emsPayload).optBoolean("silent", false)
        } else false

        val v2IsSilent = remoteMessageData?.get("ems.silent")?.let { it.toBoolean() } ?: false
        return v1IsSilent || v2IsSilent
    }

    private fun isV1Notification(remoteMessage: Map<String, String?>): Boolean {
        return remoteMessage.containsKey(MESSAGE_FILTER)
    }

    private fun isV2Notification(remoteMessage: Map<String, String?>): Boolean {
        return remoteMessage.containsKey(V2_MESSAGE_FILTER)
    }

    @JvmStatic
    fun isMobileEngageNotification(remoteMessage: Map<String, String?>): Boolean {
        return isV1Notification(remoteMessage) || isV2Notification(remoteMessage)
    }

    fun createNotification(
        context: Context,
        deviceInfo: DeviceInfo,
        fileDownloader: FileDownloader,
        notificationData: NotificationData
    ): Notification {
        val notifData = handleChannelIdMismatch(deviceInfo, notificationData, context)

        return createNotificationBuilder(notifData, context)
            .setupBuilder(
                context,
                fileDownloader,
                notifData
            )
            .styleNotification(notifData, fileDownloader, deviceInfo)
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
        fileDownloader: FileDownloader,
        notificationData: NotificationData
    ): NotificationCompat.Builder {
        val actionsData = notificationData.actions?.let { JSONArray(it) }
        val actions =
            actionsData?.let {
                NotificationActionUtils.createActions(
                    context,
                    it,
                    notificationData
                )
            } ?: listOf()
        val preloadedRemoteMessageData = createPreloadedRemoteMessageData(
            notificationData,
            getInAppDescriptor(fileDownloader, notificationData.inapp),
        )
        val resultPendingIntent = IntentUtils.createNotificationHandlerServicePendingIntent(
            context,
            preloadedRemoteMessageData,
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

    fun NotificationCompat.Builder.styleNotification(
        notificationData: NotificationData,
        fileDownloader: FileDownloader,
        deviceInfo: DeviceInfo
    ): NotificationCompat.Builder {
        val image =  ImageUtils.loadOptimizedBitmap(fileDownloader, notificationData.imageUrl, deviceInfo)
        val iconImage = ImageUtils.loadOptimizedBitmap(fileDownloader, notificationData.iconImageUrl, deviceInfo)
        return when (notificationData.style) {
            "MESSAGE" -> MessageStyle.apply(this, notificationData, image, iconImage)
            "THUMBNAIL" -> ThumbnailStyle.apply(this, notificationData, image, iconImage)
            "BIG_PICTURE" -> BigPictureStyle.apply(this, notificationData, image, iconImage)
            "BIG_TEXT" -> BigTextStyle.apply(this, notificationData, image, iconImage)
            else -> DefaultStyle.apply(this, notificationData, image, iconImage)
        }
    }

    fun getInAppDescriptor(
        fileDownloader: FileDownloader,
        inappData: String?
    ): String? {
        var result: String? = null
        try {
            if (inappData != null) {
                val inAppPayload = JSONObject(inappData)
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
        } catch (ignored: JSONException) {
        }
        return result
    }

    fun createPreloadedRemoteMessageData(
        notificationData: NotificationData,
        inAppDescriptor: String?
    ): NotificationData {
        return if (inAppDescriptor != null) {
            notificationData.copy(inapp = inAppDescriptor)
        } else {
            notificationData.copy()
        }
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