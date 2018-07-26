package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.mobileengage.iam.webview.MessageLoadedListener;

import java.util.concurrent.CountDownLatch;


public class FakeMessageLoadedListener implements MessageLoadedListener {


    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public CountDownLatch latch;
    public Mode mode;
    public int invocationCount;

    public FakeMessageLoadedListener(CountDownLatch latch) {
        this(latch, Mode.ALL_THREAD);
    }

    public FakeMessageLoadedListener(CountDownLatch latch, Mode mode) {
        this.mode = mode;
        this.latch = latch;
    }

    @Override
    public void onMessageLoaded() {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleLoaded();
        } else if (mode == Mode.ALL_THREAD) {
            handleLoaded();
        }
    }

    private void handleLoaded() {
        invocationCount++;
        if (latch != null) {
            latch.countDown();
        }
    }

    private boolean onMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
