package com.emarsys.mobileengage.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;

import java.util.Map;

public class IntentUtils {

    public static Intent createLaunchIntent(Intent remoteIntent, Context context) {
        Assert.notNull(remoteIntent, "RemoteIntent must not be null!");
        Assert.notNull(context, "Context must not be null!");
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());

        Bundle remoteExtras = remoteIntent.getExtras();
        if (remoteExtras != null) {
            intent.putExtras(remoteIntent.getExtras());
        }
        return intent;
    }

    public static Intent createTrackMessageOpenServiceIntent(
            Context context, Map<String, String> remoteMessageData,
            int notificationId,
            String action) {
        Assert.notNull(remoteMessageData, "RemoteMessageData must not be null!");
        Assert.notNull(context, "Context must not be null!");

        Intent intent = new Intent(context, TrackMessageOpenService.class);

        if (action != null) {
            intent.setAction(action);
        }

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessageData.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        bundle.putInt("notification_id", notificationId);

        intent.putExtra("payload", bundle);
        return intent;
    }

    public static PendingIntent createTrackMessageOpenServicePendingIntent(
            Context context,
            Map<String, String> remoteMessageData,
            int notificationId) {
        return createTrackMessageOpenServicePendingIntent(context, remoteMessageData, notificationId, null);
    }

    public static PendingIntent createTrackMessageOpenServicePendingIntent(
            Context context,
            Map<String, String> remoteMessageData,
            int notificationId,
            String action) {
        Assert.notNull(remoteMessageData, "RemoteMessageData must not be null!");
        Assert.notNull(context, "Context must not be null!");

        Intent intent = createTrackMessageOpenServiceIntent(context, remoteMessageData, notificationId, action);
        return PendingIntent.getService(
                context,
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                intent,
                0);
    }
}
