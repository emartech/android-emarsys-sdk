package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.content.ContextCompat;

import com.emarsys.core.api.notification.ChannelSettings;
import com.emarsys.core.api.notification.NotificationSettings;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.resource.MetaDataReader;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.FileDownloader;
import com.emarsys.core.util.ImageUtils;
import com.emarsys.core.validate.JsonObjectValidator;
import com.emarsys.mobileengage.inbox.InboxParseUtils;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.util.AndroidVersionUtils;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingServiceUtils {

    public static final String MESSAGE_FILTER = "ems_msg";
    public static final String METADATA_SMALL_NOTIFICATION_ICON_KEY = "com.emarsys.mobileengage.small_notification_icon";
    public static final String METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color";
    public static final int DEFAULT_SMALL_NOTIFICATION_ICON = com.emarsys.mobileengage.R.drawable.default_small_notification_icon;

    public static boolean handleMessage(Context context, RemoteMessage remoteMessage, DeviceInfo deviceInfo, NotificationCache notificationCache, TimestampProvider timestampProvider, FileDownloader fileDownloader) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(remoteMessage, "RemoteMessage must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(notificationCache, "NotificationCache must not be null!");
        Assert.notNull(fileDownloader, "FileDownloader must not be null!");
        boolean handled = false;
        Map<String, String> remoteData = remoteMessage.getData();

        if (MessagingServiceUtils.isMobileEngageMessage(remoteData)) {

            MessagingServiceUtils.cacheNotification(timestampProvider, notificationCache, remoteData);

            int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            Notification notification = MessagingServiceUtils.createNotification(
                    notificationId,
                    context.getApplicationContext(),
                    remoteData,
                    deviceInfo,
                    new MetaDataReader(),
                    fileDownloader);

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(notificationId, notification);

            handled = true;
        }

        return handled;
    }

    public static boolean isMobileEngageMessage(Map<String, String> remoteMessageData) {
        return remoteMessageData != null && remoteMessageData.size() > 0 && remoteMessageData.containsKey(MESSAGE_FILTER);
    }

    static Notification createNotification(
            int notificationId,
            Context context,
            Map<String, String> remoteMessageData,
            DeviceInfo deviceInfo,
            MetaDataReader metaDataReader,
            FileDownloader fileDownloader) {

        int smallIconResourceId = metaDataReader.getInt(context, METADATA_SMALL_NOTIFICATION_ICON_KEY, DEFAULT_SMALL_NOTIFICATION_ICON);
        int colorResourceId = metaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR);
        Bitmap image = ImageUtils.loadOptimizedBitmap(fileDownloader, remoteMessageData.get("image_url"), deviceInfo);
        String title = getTitle(remoteMessageData, context);
        String body = remoteMessageData.get("body");
        String channelId = remoteMessageData.get("channel_id");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && deviceInfo.isDebugMode() && !isValidChannel(deviceInfo.getNotificationSettings(), channelId)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("ems_debug", "Emarsys SDK Debug Messages", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);

            body = "DEBUG - channel_id mismatch: " + channelId + " not found!";
            channelId = "ems_debug";
            title = "Emarsys SDK";
        }

        List<Action> actions = NotificationActionUtils.createActions(context, remoteMessageData, notificationId);

        Map<String, String> preloadedRemoteMessageData = createPreloadedRemoteMessageData(remoteMessageData, getInAppDescriptor(fileDownloader, remoteMessageData));
        PendingIntent resultPendingIntent = IntentUtils.createNotificationHandlerServicePendingIntent(context, preloadedRemoteMessageData, notificationId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(smallIconResourceId)
                .setAutoCancel(false)
                .setContentIntent(resultPendingIntent);

        for (int i = 0; i < actions.size(); i++) {
            builder.addAction(actions.get(i));
        }

        if (colorResourceId != 0) {
            builder.setColor(ContextCompat.getColor(context, colorResourceId));
        }

        styleNotification(builder, title, body, image);

        return builder.build();
    }

    private static void styleNotification(NotificationCompat.Builder builder, String title, String body, Bitmap bitmap) {
        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat
                            .BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null)
                            .setBigContentTitle(title)
                            .setSummaryText(body));
        } else {
            builder.setStyle(new NotificationCompat
                    .BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title));
        }
    }

    static String getTitle(Map<String, String> remoteMessageData, Context context) {
        String title = remoteMessageData.get("title");
        if (title == null || title.isEmpty()) {
            title = getDefaultTitle(remoteMessageData, context);
        }
        return title;
    }

    static String getInAppDescriptor(FileDownloader fileDownloader, Map<String, String> remoteMessageData) {
        String result = null;

        try {
            if (remoteMessageData != null) {
                String emsPayload = remoteMessageData.get("ems");
                if (emsPayload != null) {
                    JSONObject inAppPayload = new JSONObject(emsPayload).getJSONObject("inapp");
                    List<String> errors = JsonObjectValidator
                            .from(inAppPayload)
                            .hasFieldWithType("campaign_id", String.class)
                            .hasFieldWithType("url", String.class)
                            .validate();
                    if (errors.isEmpty()) {
                        String inAppUrl = inAppPayload.getString("url");

                        JSONObject inAppDescriptor = new JSONObject();
                        inAppDescriptor.put("campaignId", inAppPayload.getString("campaign_id"));
                        inAppDescriptor.put("url", inAppUrl);
                        inAppDescriptor.put("fileUrl", fileDownloader.download(inAppUrl));

                        result = inAppDescriptor.toString();
                    }
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    static Map<String, String> createPreloadedRemoteMessageData(Map<String, String> remoteMessageData, String inAppDescriptor) {
        HashMap<String, String> preloadedRemoteMessageData = new HashMap<>(remoteMessageData);
        if (inAppDescriptor != null && AndroidVersionUtils.isKitKatOrAbove()) {
            try {
                JSONObject ems = new JSONObject(preloadedRemoteMessageData.get("ems"));
                ems.put("inapp", inAppDescriptor);
                preloadedRemoteMessageData.put("ems", ems.toString());
            } catch (JSONException ignored) {
            }
        }
        return preloadedRemoteMessageData;
    }

    private static String getDefaultTitle(Map<String, String> remoteMessageData, Context context) {
        String title = "";
        if (Build.VERSION.SDK_INT < 23) {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            title = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);

            try {
                String u = remoteMessageData.get("u");
                if (u != null) {
                    JSONObject customData = new JSONObject(u);
                    title = customData.getString("ems_default_title");
                }
            } catch (JSONException ignored) {
            }
        }
        return title;
    }

    static void cacheNotification(TimestampProvider timestampProvider, NotificationCache notificationCache, Map<String, String> remoteMessageData) {
        Assert.notNull(remoteMessageData, "RemoteMessageData must not be null!");
        notificationCache.cache(InboxParseUtils.parseNotificationFromPushMessage(timestampProvider, false, remoteMessageData));
    }

    private static boolean isValidChannel(NotificationSettings notificationSettings, String channelId) {
        for (ChannelSettings channel : notificationSettings.getChannelSettings()) {
            if (channel.getChannelId().equals(channelId)) {
                return true;
            }
        }
        return false;
    }

}
