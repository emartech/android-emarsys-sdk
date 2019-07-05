package com.emarsys.mobileengage.event;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;

import java.util.HashMap;
import java.util.Map;

public class LoggingEventServiceInternal implements EventServiceInternal {

    private final Class klass;

    public LoggingEventServiceInternal(Class klass) {
        this.klass = klass;
    }

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("event_name", eventName);
        parameters.put("event_attributes", eventAttributes);
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("event_name", eventName);
        parameters.put("event_attributes", eventAttributes);
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }
}
