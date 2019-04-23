package com.emarsys.mobileengage;

import com.emarsys.core.api.result.CompletionListener;

import java.util.Map;

public interface EventServiceInternal {
    String trackCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener);

    String trackInternalCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener);
}
