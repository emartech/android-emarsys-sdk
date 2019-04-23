package com.emarsys.mobileengage.iam;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.EventServiceInternal;
import com.emarsys.mobileengage.api.EventHandler;

import java.util.Map;

public class InAppInternal implements InAppEventHandler, EventServiceInternal {
    private final InAppEventHandlerInternal inAppEventHandlerInternal;
    private final EventServiceInternal eventServiceInternal;

    public InAppInternal(InAppEventHandlerInternal inAppEventHandlerInternal, EventServiceInternal eventServiceInternal) {
        this.inAppEventHandlerInternal = inAppEventHandlerInternal;
        this.eventServiceInternal = eventServiceInternal;
    }

    @Override
    public void pause() {
        inAppEventHandlerInternal.pause();
    }

    @Override
    public void resume() {
        inAppEventHandlerInternal.resume();
    }

    @Override
    public boolean isPaused() {
        return inAppEventHandlerInternal.isPaused();
    }

    @Override
    public void setEventHandler(EventHandler eventHandler) {
        inAppEventHandlerInternal.setEventHandler(eventHandler);
    }

    @Override
    public EventHandler getEventHandler() {
        return inAppEventHandlerInternal.getEventHandler();
    }

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        return eventServiceInternal.trackCustomEvent(eventName, eventAttributes, completionListener);
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        return eventServiceInternal.trackInternalCustomEvent(eventName, eventAttributes, completionListener);
    }
}
