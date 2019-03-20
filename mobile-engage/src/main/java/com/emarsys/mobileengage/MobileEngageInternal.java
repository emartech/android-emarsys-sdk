package com.emarsys.mobileengage;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;

import java.util.Map;

public interface MobileEngageInternal extends MobileEngageRefreshTokenInternal {
    void setPushToken(String pushToken, CompletionListener completionListener);

    void removePushToken(CompletionListener completionListener);

    String setAnonymousContact(CompletionListener completionListener);

    void setContact(String contactFieldValue, CompletionListener completionListener);

    String removeContact(CompletionListener completionListener);

    String trackCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener);

    String trackInternalCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener);

    void trackMessageOpen(Intent intent, CompletionListener completionListener);

    void trackDeviceInfo();
}
