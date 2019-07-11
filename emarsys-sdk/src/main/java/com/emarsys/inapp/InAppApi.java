package com.emarsys.inapp;

import androidx.annotation.NonNull;

import com.emarsys.mobileengage.api.EventHandler;

public interface InAppApi {

    void pause();

    void resume();

    boolean isPaused();

    void setEventHandler(@NonNull final EventHandler eventHandler);
}
