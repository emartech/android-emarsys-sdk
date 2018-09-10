package com.emarsys.mobileengage.service;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MobileEngageMessagingService extends FirebaseMessagingService {

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
