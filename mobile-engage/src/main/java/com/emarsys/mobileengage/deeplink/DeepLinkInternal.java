package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;

public interface DeepLinkInternal {
    void trackDeepLinkOpen(Activity activity, Intent intent, CompletionListener completionListener);
}
