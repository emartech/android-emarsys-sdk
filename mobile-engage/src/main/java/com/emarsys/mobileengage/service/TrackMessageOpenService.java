package com.emarsys.mobileengage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.notification.NotificationCommandFactory;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

public class TrackMessageOpenService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        EMSLogger.log(MobileEngageTopic.PUSH, "Notification was clicked");

        if (intent != null) {

            PushToInAppUtils.handlePreloadedInAppMessage(intent, DependencyInjection.getContainer());

            MessagingServiceUtils.dismissNotification(this, intent);

            NotificationActionUtils.handleAction(intent, new NotificationCommandFactory(
                    this,
                    DependencyInjection.getContainer().getMobileEngageInternal()));

            MobileEngage.trackMessageOpen(intent);
        }
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
