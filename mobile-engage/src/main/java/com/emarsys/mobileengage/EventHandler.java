package com.emarsys.mobileengage;

import android.support.annotation.Nullable;

import org.json.JSONObject;

public interface EventHandler {
    void handleEvent(String eventName, @Nullable JSONObject payload);
}
