package com.emarsys.core.connection;

public interface ConnectionChangeListener {
    void onConnectionChanged(ConnectionState connectionState, boolean isConnected);
}
