package com.emarsys.predict.fake;

import android.os.Looper;

import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;

import java.util.concurrent.CountDownLatch;


public class FakeResultListener<T> implements ResultListener<Try<T>> {

    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public int successCount;
    public T resultStatus;
    public Throwable errorCause;
    public int errorCount;
    public CountDownLatch latch;
    public Mode mode;

    public FakeResultListener(CountDownLatch latch) {
        this(latch, Mode.ALL_THREAD);
    }

    public FakeResultListener(CountDownLatch latch, Mode mode) {
        this.mode = mode;
        this.latch = latch;
    }

    @Override
    public void onResult(Try<T> result) {
        if (result.getResult() != null) {
            onSuccess(result.getResult());
        }
        if (result.getErrorCause() != null) {
            onError(result.getErrorCause());
        }
    }

    private void onSuccess(T result) {
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

    private void handleSuccess(T result) {
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