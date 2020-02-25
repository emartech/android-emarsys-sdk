package com.emarsys.push;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.mobileengage.api.event.EventHandler;

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

    void clearPushToken();

    void clearPushToken(final CompletionListener completionListener);

    void setNotificationEventHandler(EventHandler notificationEventHandler);

    void setSilentMessageEventHandler(EventHandler silentMesssageEventHandler);
}
