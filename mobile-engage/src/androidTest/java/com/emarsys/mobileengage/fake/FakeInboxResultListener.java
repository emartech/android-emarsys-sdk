package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

import java.util.concurrent.CountDownLatch;


public class FakeInboxResultListener implements InboxResultListener<NotificationInboxStatus> {

    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public int successCount;
    public NotificationInboxStatus resultStatus;
    public Exception errorCause;
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
    public void onSuccess(NotificationInboxStatus result) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleSuccess(result);
        } else if (mode == Mode.ALL_THREAD) {
            handleSuccess(result);
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

    private void handleSuccess(NotificationInboxStatus result) {
        resultStatus = result;
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