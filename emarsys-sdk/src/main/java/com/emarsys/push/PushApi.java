package com.emarsys.push;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.api.push.NotificationInformationListener;

public interface PushApi {
    /**
     * @deprecated This method is not necessary anymore. EmarsysMessagingService takes care of it.
     * Use EmarsysMessagingService.handleMessage() instead!
     */
    @Deprecated
    void trackMessageOpen(@NonNull final Intent intent);

    /**
     * @deprecated This method is not necessary anymore. EmarsysMessagingService takes care of it.
     * Use EmarsysMessagingService.handleMessage() instead!
     */
    @Deprecated
    void trackMessageOpen(
            @NonNull final Intent intent,
            @NonNull final CompletionListener completionListener);

    void setPushToken(@NonNull final String pushToken);

    void setPushToken(
            @NonNull final String pushToken,
            @NonNull final CompletionListener completionListener);

    String getPushToken();

    void clearPushToken();

    void clearPushToken(@NonNull final CompletionListener completionListener);

    void setNotificationEventHandler(@NonNull EventHandler notificationEventHandler);

    void setSilentMessageEventHandler(@NonNull EventHandler silentMessageEventHandler);

    void setNotificationInformationListener(@NonNull NotificationInformationListener notificationInformationListener);

    void setSilentNotificationInformationListener(@NonNull NotificationInformationListener silentNotificationInformationListener);
}
