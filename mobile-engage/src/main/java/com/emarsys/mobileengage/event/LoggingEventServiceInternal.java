package com.emarsys.mobileengage.event;

import com.emarsys.core.api.result.CompletionListener;

import java.util.Map;

public class LoggingEventServiceInternal implements EventServiceInternal {
    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        return null;
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        return null;
    }
}
