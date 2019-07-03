package com.emarsys.service;

import com.emarsys.Emarsys;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class EmarsysMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (DependencyInjection.getContainer().getDeviceInfo().isAutomaticPushSendingEnabled()) {
            Emarsys.Push.setPushToken(token);
        }
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        DependencyContainer container = DependencyInjection.<MobileEngageDependencyContainer>getContainer();
        container.getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                EmarsysMessagingServiceUtils.handleMessage(EmarsysMessagingService.this, remoteMessage);
            }
        });
    }
}
