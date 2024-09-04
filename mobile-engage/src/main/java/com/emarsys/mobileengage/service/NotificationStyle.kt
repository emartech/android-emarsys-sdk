package com.emarsys.mobileengage.service

import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat

sealed class NotificationStyle {
    abstract fun apply(
        builder: NotificationCompat.Builder,
        notificationData: NotificationData,
        image: Bitmap?,
        iconImage: Bitmap?
    ): NotificationCompat.Builder
}

object ThumbnailStyle : NotificationStyle() {
    override fun apply(
        builder: NotificationCompat.Builder,
        notificationData: NotificationData,
        image: Bitmap?,
        iconImage: Bitmap?
    ): NotificationCompat.Builder {
        return builder.setLargeIcon(image)
                .setContentTitle(notificationData.title)
                .setContentText(notificationData.body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(notificationData.body)
                        .setBigContentTitle(notificationData.title))
    }
}

object MessageStyle : NotificationStyle() {
    override fun apply(
        builder: NotificationCompat.Builder,
        notificationData: NotificationData,
        image: Bitmap?,
        iconImage: Bitmap?
    ): NotificationCompat.Builder {
        val user = Person.Builder()
            .setName(notificationData.title)
            .setIcon(IconCompat.createWithAdaptiveBitmap(image!!)).build()
        return builder.setStyle(NotificationCompat.MessagingStyle(user)
                .addMessage(notificationData.body, System.currentTimeMillis(), user)
                .setGroupConversation(false))
    }
}

object BigPictureStyle : NotificationStyle() {
    override fun apply(
        builder: NotificationCompat.Builder,
        notificationData: NotificationData,
        image: Bitmap?,
        iconImage: Bitmap?
    ): NotificationCompat.Builder {
        return builder.setLargeIcon(iconImage)
                .setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(image)
                        .setBigContentTitle(notificationData.title)
                        .setSummaryText(notificationData.body))
    }
}

object BigTextStyle : NotificationStyle() {
    override fun apply(
        builder: NotificationCompat.Builder,
        notificationData: NotificationData,
        image: Bitmap?,
        iconImage: Bitmap?
    ): NotificationCompat.Builder {
        return builder.setStyle(NotificationCompat.BigTextStyle()
                .bigText(notificationData.body)
                .setBigContentTitle(notificationData.title))
    }
}

object DefaultStyle : NotificationStyle() {
    override fun apply(
        builder: NotificationCompat.Builder,
        notificationData: NotificationData,
        image: Bitmap?,
        iconImage: Bitmap?
    ): NotificationCompat.Builder {
        if (notificationData.imageUrl != null) {
            builder.setLargeIcon(image)
                    .setStyle(NotificationCompat.BigPictureStyle()
                            .bigPicture(image)
                        .bigLargeIcon(null as Bitmap?)
                            .setBigContentTitle(notificationData.title)
                            .setSummaryText(notificationData.body))
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle()
                    .bigText(notificationData.body)
                    .setBigContentTitle(notificationData.title))
        }
        return builder
    }

}