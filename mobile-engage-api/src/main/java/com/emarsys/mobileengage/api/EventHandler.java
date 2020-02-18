package com.emarsys.mobileengage.api;

import androidx.annotation.Nullable;

import org.json.JSONObject;

/**
 * @deprecated will be removed in 3.0.0, replaced by {@link com.emarsys.mobileengage.api.event.EventHandler}
 */
@Deprecated
public interface EventHandler {
    void handleEvent(String eventName, @Nullable JSONObject payload);
}
