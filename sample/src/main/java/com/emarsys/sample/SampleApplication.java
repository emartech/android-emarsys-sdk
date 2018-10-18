package com.emarsys.sample;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.emarsys.Emarsys;
import com.emarsys.config.EmarsysConfig;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;

import org.json.JSONObject;

public class SampleApplication extends Application implements EventHandler, NotificationEventHandler {

    private static final String TAG = "SampleApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Emarsys SDK version: " + com.emarsys.BuildConfig.VERSION_NAME);

        EmarsysConfig config = new EmarsysConfig.Builder()
                .application(this)
                .mobileEngageCredentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .contactFieldId(3)
                .predictMerchantId("merchantId")
                .enableDefaultChannel("default", "here is a description")
                .inAppEventHandler(this)
                .enableExperimentalFeatures(MobileEngageFeature.IN_APP_MESSAGING, MobileEngageFeature.USER_CENTRIC_INBOX)
                .notificationEventHandler(this)
                .build();

        createNotificationChannels();

        Emarsys.setup(config);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel("ems_sample_news", "News", "News and updates go into this channel", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("ems_sample_messages", "Messages", "Important messages go into this channel", NotificationManager.IMPORTANCE_HIGH);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String id, String name, String description, int importance) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void handleEvent(String eventName, JSONObject payload) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleEvent(Context context, String eventName, @Nullable JSONObject payload) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show();
    }
}