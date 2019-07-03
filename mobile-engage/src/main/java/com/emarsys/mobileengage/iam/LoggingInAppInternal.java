package com.emarsys.mobileengage.iam;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.mobileengage.api.EventHandler;

import java.util.Map;

public class LoggingInAppInternal implements InAppInternal {
    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        return null;
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        return null;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void setEventHandler(EventHandler eventHandler) {

    }

    @Override
    public EventHandler getEventHandler() {
        return null;
    }
}
