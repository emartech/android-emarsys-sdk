package com.emarsys.mobileengage;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;

import java.util.Map;

public class MobileEngageInternalV3 implements MobileEngageInternal {
    @Override
    public void setPushToken(String pushToken) {

    }

    @Override
    public void removePushToken() {

    }

    @Override
    public String setAnonymousContact(CompletionListener completionListener) {
        return null;
    }

    @Override
    public String setContact(String contactFieldValue, CompletionListener completionListener) {
        return null;
    }

    @Override
    public String removeContact(CompletionListener completionListener) {
        return null;
    }

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        return null;
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        return null;
    }

    @Override
    public String trackMessageOpen(Intent intent, CompletionListener completionListener) {
        return null;
    }
}
