package com.emarsys.mobileengage.iam;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;
import com.emarsys.mobileengage.api.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class LoggingInAppInternal implements InAppInternal {

    private static final Class<LoggingInAppInternal> KLASS = LoggingInAppInternal.class;

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("event_name", eventName);
        parameters.put("event_attributes", eventAttributes);
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("event_name", eventName);
        parameters.put("event_attributes", eventAttributes);
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, parameters));
        return null;
    }

    @Override
    public void pause() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, null));
    }

    @Override
    public void resume() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, null));
    }

    @Override
    public boolean isPaused() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, null));
        return false;
    }

    @Override
    public void setEventHandler(EventHandler eventHandler) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("event_handler", eventHandler != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, parameters));
    }

    @Override
    public EventHandler getEventHandler() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(KLASS, callerMethodName, null));
        return null;
    }
}
