package com.emarsys.service;

import com.emarsys.Emarsys;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.service.MessagingServiceUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class EmarsysMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        EMSLogger.log(MobileEngageTopic.PUSH, "New token: %s", token);

        Emarsys.Push.setPushToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        boolean handled = MessagingServiceUtils.handleMessage(
                this,
                remoteMessage,
                DependencyInjection.<MobileEngageDependencyContainer>getContainer().getOreoConfig());

        EMSLogger.log(MobileEngageTopic.PUSH, "Remote message handled by MobileEngage: %b", handled);
    }
}
