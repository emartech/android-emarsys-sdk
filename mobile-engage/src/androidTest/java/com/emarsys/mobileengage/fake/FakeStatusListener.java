package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.mobileengage.MobileEngageStatusListener;

import java.util.concurrent.CountDownLatch;

public class FakeStatusListener implements MobileEngageStatusListener {

    public enum Mode {
        MAIN_THREAD, ALL_THREAD
    }

    public CountDownLatch latch;
    public Mode mode;

    public int onStatusLogCount;
    public int onErrorCount;
    public String errorId;
    public String successId;
    public Exception errorCause;
    public String successLog;

    public FakeStatusListener() {
        this(null);
    }

    public FakeStatusListener(CountDownLatch latch) {
        this(latch, Mode.ALL_THREAD);
    }

    public FakeStatusListener(CountDownLatch latch, Mode mode) {
        this.latch = latch;
        this.mode = mode;
    }

    @Override
    public void onError(String id, Exception cause) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleError(id, cause);
        } else if (mode == Mode.ALL_THREAD) {
            handleError(id, cause);
        }
    }

    @Override
    public void onStatusLog(String id, String log) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleLog(id, log);
        } else if (mode == Mode.ALL_THREAD) {
            handleLog(id, log);
        }
    }

    private void handleError(String id, Exception cause) {
        errorId = id;
        errorCause = cause;
        onErrorCount++;
        if (latch != null) {
            latch.countDown();
        }
    }

    private void handleLog(String id, String log) {
        successId = id;
        successLog = log;
        onStatusLogCount++;
        if (latch != null) {
            latch.countDown();
        }
    }

    private boolean onMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
