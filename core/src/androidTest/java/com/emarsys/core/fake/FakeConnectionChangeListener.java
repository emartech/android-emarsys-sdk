package com.emarsys.core.fake;

import com.emarsys.core.connection.ConnectionChangeListener;
import com.emarsys.core.connection.ConnectionState;

import java.util.concurrent.CountDownLatch;

public class FakeConnectionChangeListener implements ConnectionChangeListener {

    public int onConnectionChangedCount;
    public String threadName;
    public CountDownLatch latch;
    public ConnectionState connectionState;
    public boolean isConnected;

    public FakeConnectionChangeListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onConnectionChanged(ConnectionState connectionState, boolean isConnected) {
        this.connectionState = connectionState;
        this.isConnected = isConnected;
        onConnectionChangedCount++;
        threadName = Thread.currentThread().getName();
        latch.countDown();
    }
}
