package com.emarsys.mobileengage.notification;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONObject;

public interface NotificationEventHandler {
    void handleEvent(Context context, String eventName, @Nullable JSONObject payload);
}
