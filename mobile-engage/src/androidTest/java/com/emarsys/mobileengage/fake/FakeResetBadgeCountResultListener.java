package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;

import java.util.concurrent.CountDownLatch;

public class FakeResetBadgeCountResultListener implements ResetBadgeCountResultListener {

    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public int successCount;
    public Exception errorCause;
    public int errorCount;
    public CountDownLatch latch;
    public Mode mode;

    public FakeResetBadgeCountResultListener(CountDownLatch latch) {
        this(latch, Mode.ALL_THREAD);
    }

    public FakeResetBadgeCountResultListener(CountDownLatch latch, Mode mode) {
        this.mode = mode;
        this.latch = latch;
    }

    @Override
    public void onSuccess() {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleSuccess();
        } else if (mode == Mode.ALL_THREAD) {
            handleSuccess();
        }
    }

    @Override
    public void onError(Exception cause) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleError(cause);
        } else if (mode == Mode.ALL_THREAD) {
            handleError(cause);
        }
    }

    private void handleSuccess() {
        successCount++;
        if (latch != null) {
            latch.countDown();
        }
    }

    private void handleError(Exception cause) {
        errorCount++;
        errorCause = cause;
        if (latch != null) {
            latch.countDown();
        }
    }

    private boolean onMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
