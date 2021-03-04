package com.emarsys.mobileengage.push;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.api.push.NotificationInformationListener;

public interface PushInternal {
    void setPushToken(String pushToken, CompletionListener completionListener);

    String getPushToken();

    void clearPushToken(CompletionListener completionListener);

    void trackMessageOpen(Intent intent, CompletionListener completionListener);

    void setNotificationEventHandler(EventHandler notificationEventHandler);

    void setSilentMessageEventHandler(EventHandler silentMessageEventHandler);

    void setNotificationInformationListener(NotificationInformationListener notificationInformationListener);

    void setSilentNotificationInformationListener(NotificationInformationListener silentNotificationInformationListener);
}
