package com.emarsys.mobileengage.iam;

import com.emarsys.mobileengage.api.event.EventHandler;


public class InAppEventHandlerInternal implements InAppEventHandler {

    private boolean isPaused;
    private EventHandler eventHandler;

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

}
