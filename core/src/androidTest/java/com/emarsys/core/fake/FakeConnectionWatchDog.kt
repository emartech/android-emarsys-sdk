package com.emarsys.core.fake;

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory;
import com.emarsys.core.connection.ConnectionChangeListener;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.testUtil.InstrumentationRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FakeConnectionWatchDog extends ConnectionWatchDog {

    List<Boolean> isConnectedReplies;
    public ConnectionChangeListener connectionChangeListener;
    public CountDownLatch latch;

    public FakeConnectionWatchDog(CountDownLatch latch, Boolean... isConnectedReplies) {
        super(InstrumentationRegistry.getTargetContext(), ConcurrentHandlerHolderFactory.INSTANCE.create());
        this.latch = latch;
        this.isConnectedReplies = new ArrayList<>(Arrays.asList(isConnectedReplies));
    }

    @Override
    public boolean isConnected() {
        boolean result = isConnectedReplies.get(0);
        if (isConnectedReplies.size() > 1) {
            isConnectedReplies.remove(0);
        }

        latch.countDown();
        return result;
    }

    @Override
    public void registerReceiver(ConnectionChangeListener connectionChangeListener) {
        this.connectionChangeListener = connectionChangeListener;
    }
}
