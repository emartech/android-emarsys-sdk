package com.emarsys.core.fake;

import android.os.Looper;

import com.emarsys.core.request.factory.RunnableFactory;

import java.util.concurrent.CountDownLatch;


public class FakeRunnableFactory implements RunnableFactory {

    public CountDownLatch latch;
    public boolean checkNonUIThread;
    public int executionCount;

    public FakeRunnableFactory(CountDownLatch latch, boolean checkNonUIThread) {
        this.latch = latch;
        this.checkNonUIThread = checkNonUIThread;
    }

    public FakeRunnableFactory(CountDownLatch latch) {
        this(latch, false);
    }

    @Override
    public Runnable runnableFrom(final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                runnable.run();
                if (checkNonUIThread) {
                    if (isCoreSDKHandlerThread()) {
                        executionCount++;
                    }
                } else {
                    executionCount++;
                }
                latch.countDown();
            }
        };
    }

    private boolean isCoreSDKHandlerThread() {
        return Looper.myLooper() != null &&
                Looper.myLooper().getThread().getName().startsWith("CoreSDKHandlerThread");
    }
}
