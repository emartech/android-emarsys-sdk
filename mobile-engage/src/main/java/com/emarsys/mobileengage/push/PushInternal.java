package com.emarsys.mobileengage.push;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;

public interface PushInternal {
    void setPushToken(String pushToken, CompletionListener completionListener);

    void clearPushToken(CompletionListener completionListener);

    void trackMessageOpen(Intent intent, CompletionListener completionListener);

}
