package com.emarsys.mobileengage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.notification.NotificationCommandFactory;

public class NotificationHandlerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            NotificationActionUtils.handleAction(intent, new NotificationCommandFactory(
                    this,
                    DependencyInjection.<MobileEngageDependencyContainer>getContainer()));

        }
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
