package com.emarsys.core;

public interface StatusListener {
    void onError(String id, Exception cause);

    void onStatusLog(String id, String log);
}
