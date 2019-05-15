package com.emarsys.mobileengage;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;

import java.util.Map;

public interface MobileEngageInternal extends MobileEngageClientInternal {
    void setPushToken(String pushToken, CompletionListener completionListener);

    void clearPushToken(CompletionListener completionListener);

    void setContact(String contactFieldValue, CompletionListener completionListener);

    void clearContact(CompletionListener completionListener);

    String trackCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener);

    String trackInternalCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener);

    void trackMessageOpen(Intent intent, CompletionListener completionListener);
}
