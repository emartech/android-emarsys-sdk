package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.content.ContextCompat;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.core.resource.MetaDataReader;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.FileUtils;
import com.emarsys.core.util.ImageUtils;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.core.validate.JsonObjectValidator;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.inbox.InboxParseUtils;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.util.AndroidVersionUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
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

    static NotificationCache notificationCache = new NotificationCache();

    public static boolean handleMessage(Context context, RemoteMessage remoteMessage, DeviceInfo deviceInfo) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(remoteMessage, "RemoteMessage must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        boolean handled = false;
        Map<String, String> remoteData = remoteMessage.getData();

        EMSLogger.log(MobileEngageTopic.PUSH, "Remote message data %s", remoteData);

        if (MessagingServiceUtils.isMobileEngageMessage(remoteData)) {

            EMSLogger.log(MobileEngageTopic.PUSH, "RemoteMessage is ME message");

            MessagingServiceUtils.cacheNotification(remoteData);

            int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            Notification notification = MessagingServiceUtils.createNotification(
                    notificationId,
                    context.getApplicationContext(),
                    remoteData,
                    deviceInfo,
                    new MetaDataReader());

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(notificationId, notification);

            handled = true;
        }

        return handled;
    }

    public static boolean isMobileEngageMessage(Map<String, String> remoteMessageData) {
        return remoteMessageData != null && remoteMessageData.size() > 0 && remoteMessageData.containsKey(MESSAGE_FILTER);
    }

    static void dismissNotification(Context context, Intent intent) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(intent, "Intent must not be null!");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bundle bundle = intent.getBundleExtra("payload");
        if (bundle != null) {
            int notificationId = bundle.getInt("notification_id", Integer.MIN_VALUE);
            if (notificationId != Integer.MIN_VALUE) {
                manager.cancel(notificationId);
            }
        }
    }

    static Notification createNotification(
            int notificationId,
            Context context,
            Map<String, String> remoteMessageData,
            DeviceInfo deviceInfo,
            MetaDataReader metaDataReader) {

        int smallIconResourceId = metaDataReader.getInt(context, METADATA_SMALL_NOTIFICATION_ICON_KEY, DEFAULT_SMALL_NOTIFICATION_ICON);
        int colorResourceId = metaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR);
        Bitmap image = ImageUtils.loadOptimizedBitmap(context, remoteMessageData.get("image_url"), deviceInfo);
        String title = getTitle(remoteMessageData, context);
        String body = remoteMessageData.get("body");
        String channelId = remoteMessageData.get("channel_id");
        List<Action> actions = NotificationActionUtils.createActions(context, remoteMessageData, notificationId);

        Map<String, String> preloadedRemoteMessageData = createPreloadedRemoteMessageData(remoteMessageData, getInAppDescriptor(context, remoteMessageData));
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

    static String getInAppDescriptor(Context context, Map<String, String> remoteMessageData) {
        String result = null;

        try {
            if (remoteMessageData != null) {
                String emsPayload = remoteMessageData.get("ems");
                if (emsPayload != null) {
                    JSONObject inAppPayload = new JSONObject(emsPayload).getJSONObject("inapp");
                    List<String> errors = JsonObjectValidator
                            .from(inAppPayload)
                            .hasFieldWithType("campaignId", String.class)
                            .hasFieldWithType("url", String.class)
                            .validate();
                    if (errors.isEmpty()) {
                        String inAppUrl = inAppPayload.getString("url");

                        JSONObject inAppDescriptor = new JSONObject();
                        inAppDescriptor.put("campaignId", inAppPayload.getString("campaignId"));
                        inAppDescriptor.put("url", inAppUrl);
                        inAppDescriptor.put("fileUrl", FileUtils.download(context, inAppUrl));

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

    static void cacheNotification(Map<String, String> remoteMessageData) {
        Assert.notNull(remoteMessageData, "RemoteMessageData must not be null!");
        boolean isUserCentric = ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.USER_CENTRIC_INBOX);
        notificationCache.cache(InboxParseUtils.parseNotificationFromPushMessage(remoteMessageData, isUserCentric));
    }

}
