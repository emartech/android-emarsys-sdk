package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.core.api.result.CompletionListener;

import java.util.concurrent.CountDownLatch;

public class FakeResetBadgeCountResultListener implements CompletionListener {


    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public int successCount;
    public Throwable errorCause;
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
    public void onCompleted(Throwable errorCause) {
        if (errorCause != null) {
            onError(errorCause);
        } else {
            onSuccess();
        }
    }

    private void onSuccess() {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleSuccess();
        } else if (mode == Mode.ALL_THREAD) {
            handleSuccess();
        }
    }

    private void onError(Throwable cause) {
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

    private void handleError(Throwable cause) {
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
