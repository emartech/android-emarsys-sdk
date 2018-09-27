package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

import java.util.concurrent.CountDownLatch;


public class FakeInboxResultListener implements ResultListener<Try<NotificationInboxStatus>> {

    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public int successCount;
    public NotificationInboxStatus resultStatus;
    public Throwable errorCause;
    public int errorCount;
    public CountDownLatch latch;
    public Mode mode;

    public FakeInboxResultListener(CountDownLatch latch) {
        this(latch, Mode.ALL_THREAD);
    }

    public FakeInboxResultListener(CountDownLatch latch, Mode mode) {
        this.mode = mode;
        this.latch = latch;
    }

    @Override
    public void onResult(Try<NotificationInboxStatus> result) {
        if (result.getResult() != null) {
            onSuccess(result.getResult());
        }
        if (result.getErrorCause() != null) {
            onError(result.getErrorCause());
        }
    }

    private void onSuccess(NotificationInboxStatus result) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleSuccess(result);
        } else if (mode == Mode.ALL_THREAD) {
            handleSuccess(result);
        }
    }

    private void onError(Throwable cause) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleError(cause);
        } else if (mode == Mode.ALL_THREAD) {
            handleError(cause);
        }
    }

    private void handleSuccess(NotificationInboxStatus result) {
        resultStatus = result;
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