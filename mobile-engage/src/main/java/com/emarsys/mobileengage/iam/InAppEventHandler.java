package com.emarsys.mobileengage.iam;

import com.emarsys.mobileengage.api.EventHandler;

public interface InAppEventHandler {
    void pause();

    void resume();

    boolean isPaused();

    void setEventHandler(EventHandler eventHandler);

    EventHandler getEventHandler();
}
