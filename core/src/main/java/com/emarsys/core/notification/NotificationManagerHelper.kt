package com.emarsys.core.notification

import android.os.Build
import androidx.annotation.RequiresApi
import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.core.api.notification.NotificationSettings
import java.util.*

class NotificationManagerHelper(private val notificationManagerProxy: NotificationManagerProxy) :
    NotificationSettings {

    override val areNotificationsEnabled: Boolean
        get() = notificationManagerProxy.areNotificationsEnabled

    override val importance: Int
        get() = notificationManagerProxy.importance

    @get:RequiresApi(api = Build.VERSION_CODES.O)
    override val channelSettings: List<ChannelSettings>
        get() = notificationManagerProxy.notificationChannels

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as NotificationManagerHelper
        return notificationManagerProxy == that.notificationManagerProxy
    }

    override fun hashCode(): Int {
        return Objects.hash(notificationManagerProxy)
    }
}