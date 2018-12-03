package com.emarsys.mobileengage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.notification.NotificationCommandFactory;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

public class NotificationHandlerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        EMSLogger.log(MobileEngageTopic.PUSH, "Notification was clicked");

        if (intent != null) {

            MobileEngageDependencyContainer container = DependencyInjection.getContainer();

            PushToInAppUtils.handlePreloadedInAppMessage(intent, container);

            container.getMobileEngageInternal().trackMessageOpen(intent, null);

            NotificationActionUtils.handleAction(intent, new NotificationCommandFactory(
                    this,
                    container.getMobileEngageInternal(),
                    container.getNotificationEventHandler()));

        }
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
