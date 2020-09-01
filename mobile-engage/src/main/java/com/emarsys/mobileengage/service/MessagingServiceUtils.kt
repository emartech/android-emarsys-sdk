package com.emarsys.mobileengage.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.emarsys.core.Mockable
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.ImageUtils.loadOptimizedBitmap
import com.emarsys.core.validate.JsonObjectValidator
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.inbox.InboxParseUtils
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.SilentNotificationInformationCommand
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

@Mockable
object MessagingServiceUtils {
    const val MESSAGE_FILTER = "ems_msg"
    private const val METADATA_SMALL_NOTIFICATION_ICON_KEY = "com.emarsys.mobileengage.small_notification_icon"
    private const val METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color"
    private val DEFAULT_SMALL_NOTIFICATION_ICON = R.drawable.default_small_notification_icon

    @JvmStatic
    fun handleMessage(context: Context,
                      remoteMessage: RemoteMessage,
                      deviceInfo: DeviceInfo,
                      notificationCache: NotificationCache,
                      timestampProvider: TimestampProvider?,
                      fileDownloader: FileDownloader,
                      actionCommandFactory: ActionCommandFactory): Boolean {

        var handled = false
        val remoteData = remoteMessage.data
        if (isMobileEngageMessage(remoteData)) {
            if (isSilent(remoteData)) {
                createSilentPushCommands(actionCommandFactory, remoteData).forEach {
                    Handler(Looper.getMainLooper()).post {
                        it?.run()
                    }
                }
            } else {
                cacheNotification(timestampProvider, notificationCache, remoteData)
                val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                val notification = createNotification(
                        notificationId,
                        context.applicationContext,
                        remoteData,
                        deviceInfo,
                        MetaDataReader(),
                        fileDownloader)
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(notificationId, notification)
            }
            handled = true
        }
        return handled
    }

    fun createSilentPushCommands(actionCommandFactory: ActionCommandFactory, remoteMessageData: Map<String, String?>): List<Runnable?> {
        val actionsJsonArray = JSONObject(remoteMessageData["ems"]).optJSONArray("actions")
        val actions: MutableList<Runnable?> = mutableListOf()
        val campaignId = JSONObject(remoteMessageData["ems"]).optString("multichannelId")
        if (campaignId.isNotEmpty()) {
            val silentNotificationInformationListenerProvider = DependencyInjection.getContainer<MobileEngageDependencyContainer>().getSilentNotificationInformationListenerProvider()
            actions.add(SilentNotificationInformationCommand(silentNotificationInformationListenerProvider, NotificationInformation(campaignId)))
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
    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>?): Boolean {
        return remoteMessageData != null && remoteMessageData.isNotEmpty() && remoteMessageData.containsKey(MESSAGE_FILTER)
    }

    fun createNotification(
            notificationId: Int,
            context: Context,
            remoteMessageData: Map<String, String>,
            deviceInfo: DeviceInfo,
            metaDataReader: MetaDataReader,
            fileDownloader: FileDownloader): Notification {
        val smallIconResourceId = metaDataReader.getInt(context, METADATA_SMALL_NOTIFICATION_ICON_KEY, DEFAULT_SMALL_NOTIFICATION_ICON)
        val colorResourceId = metaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR)
        val image = loadOptimizedBitmap(fileDownloader, remoteMessageData["image_url"], deviceInfo)
        val iconImage = loadOptimizedBitmap(fileDownloader, remoteMessageData["icon_url"], deviceInfo)
        var title = getTitle(remoteMessageData, context)
        val style = JSONObject(remoteMessageData["ems"] ?: "{}").optString("style")
        var body = remoteMessageData["body"]
        var channelId = remoteMessageData["channel_id"]
        if (AndroidVersionUtils.isOreoOrAbove() && deviceInfo.isDebugMode && !isValidChannel(deviceInfo.notificationSettings, channelId)) {
            body = "DEBUG - channel_id mismatch: $channelId not found!"
            channelId = createDebugChannel(context)
            title = "Emarsys SDK"
        }
        val actions = NotificationActionUtils.createActions(context, remoteMessageData, notificationId)
        val preloadedRemoteMessageData = createPreloadedRemoteMessageData(remoteMessageData, getInAppDescriptor(fileDownloader, remoteMessageData))
        val resultPendingIntent = IntentUtils.createNotificationHandlerServicePendingIntent(context, preloadedRemoteMessageData, notificationId)
        val builder = if (channelId == null) NotificationCompat.Builder(context) else NotificationCompat.Builder(context, channelId)
        builder
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(smallIconResourceId)
                .setAutoCancel(false)
                .setContentIntent(resultPendingIntent)
        for (i in actions.indices) {
            builder.addAction(actions[i])
        }
        if (colorResourceId != 0) {
            builder.color = ContextCompat.getColor(context, colorResourceId)
        }
        styleNotification(builder, title, body, style, image, iconImage)
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDebugChannel(context: Context): String {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel("ems_debug", "Emarsys SDK Debug Messages", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        return notificationChannel.id
    }

    fun styleNotification(builder: NotificationCompat.Builder, title: String?, body: String?, style: String?, bitmap: Bitmap?, icon : Bitmap?) {
        var internalStyle = style
        if (internalStyle != null) {
            val styleToApply = when (internalStyle) {
                "MESSAGE" -> {
                    val user = Person.Builder()
                            .setName(title)
                            .setIcon(IconCompat.createWithAdaptiveBitmap(bitmap)).build()
                    NotificationCompat.MessagingStyle(user)
                            .addMessage(body, System.currentTimeMillis(), user)
                            .setGroupConversation(false)
                }
                "THUMBNAIL" -> {
                    builder.setLargeIcon(bitmap)
                            .setContentTitle(title)
                            .setContentText(body)
                    null
                }
                "BIG_PICTURE" -> {
                    builder.setLargeIcon(icon)
                    NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setBigContentTitle(title)
                            .setSummaryText(body)
                }
                "BIG_TEXT" -> {
                    NotificationCompat.BigTextStyle()
                            .bigText(body)
                            .setBigContentTitle(title)
                }
                else -> {
                    internalStyle = null
                    null
                }
            }
            if (styleToApply != null)
                builder.setStyle(styleToApply)
        }
        if (internalStyle == null){
            if (bitmap != null) {
                builder.setLargeIcon(bitmap)
                        .setStyle(NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null)
                                .setBigContentTitle(title)
                                .setSummaryText(body))
            } else {
                builder.setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body)
                        .setBigContentTitle(title))
            }
        }
    }

    fun getTitle(remoteMessageData: Map<String, String?>, context: Context): String? {
        var title = remoteMessageData["title"]
        if (title == null || title.isEmpty()) {
            title = getDefaultTitle(context)
        }
        return title
    }

    fun getInAppDescriptor(fileDownloader: FileDownloader, remoteMessageData: Map<String, String?>?): String? {
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
                        val inAppUrl = inAppPayload.getString("url")
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

    fun createPreloadedRemoteMessageData(remoteMessageData: Map<String, String?>, inAppDescriptor: String?): Map<String, String?> {
        val preloadedRemoteMessageData = mutableMapOf<String, String?>()
        val keys = remoteMessageData.keys
        for (key in keys) {
            preloadedRemoteMessageData[key] = remoteMessageData[key]
        }
        if (inAppDescriptor != null && AndroidVersionUtils.isKitKatOrAbove()) {
            try {
                val ems = JSONObject(preloadedRemoteMessageData["ems"])
                ems.put("inapp", inAppDescriptor)
                preloadedRemoteMessageData["ems"] = ems.toString()
            } catch (ignored: JSONException) {
            }
        }
        return preloadedRemoteMessageData
    }

    private fun getDefaultTitle(context: Context): String? {
        var title: String? = null
        if (AndroidVersionUtils.isBelowMarshmallow()) {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes

            title = if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
        }
        return title
    }

    fun cacheNotification(timestampProvider: TimestampProvider?, notificationCache: NotificationCache, remoteMessageData: Map<String, String?>) {
        notificationCache.cache(InboxParseUtils.parseNotificationFromPushMessage(timestampProvider, false, remoteMessageData))
    }

    private fun isValidChannel(notificationSettings: NotificationSettings, channelId: String?): Boolean {
        return notificationSettings.channelSettings.any { it.channelId == channelId }
    }
}