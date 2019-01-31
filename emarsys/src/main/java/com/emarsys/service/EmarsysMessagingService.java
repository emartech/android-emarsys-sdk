package com.emarsys.service;

import com.emarsys.Emarsys;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class EmarsysMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Emarsys.Push.setPushToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        EmarsysMessagingServiceUtils.handleMessage(this, remoteMessage);
    }
}
