package com.emarsys.core.notification

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.emarsys.core.Mockable
import com.emarsys.core.api.notification.ChannelSettings
import java.util.Objects

@Mockable
class NotificationManagerProxy(
    private val notificationManager: NotificationManager,
    private val notificationManagerCompat: NotificationManagerCompat
) {
    val areNotificationsEnabled: Boolean
        get() = notificationManagerCompat.areNotificationsEnabled()

    val importance: Int
        get() = notificationManagerCompat.importance

    @get:RequiresApi(api = Build.VERSION_CODES.O)
    val notificationChannels: List<ChannelSettings>
        get() {
            val channelSettings: MutableList<ChannelSettings> = ArrayList()
            for (notificationChannel in notificationManager.notificationChannels) {
                channelSettings.add(
                    ChannelSettings(
                        notificationChannel.id,
                        notificationChannel.importance,
                        notificationChannel.canBypassDnd(),
                        notificationChannel.canShowBadge(),
                        notificationChannel.shouldVibrate(),
                        notificationChannel.shouldShowLights()
                    )
                )
            }
            return channelSettings
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as NotificationManagerProxy
        return notificationManager == that.notificationManager &&
                notificationManagerCompat == that.notificationManagerCompat &&
                areNotificationsEnabled == that.areNotificationsEnabled
    }

    override fun hashCode(): Int {
        return Objects.hash(
            notificationManager,
            notificationManagerCompat,
            areNotificationsEnabled
        )
    }
}