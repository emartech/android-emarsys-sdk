package com.emarsys.core.concurrency;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

public class CoreSdkHandler extends Handler {

    public CoreSdkHandler(HandlerThread handlerThread) {
        super(handlerThread.getLooper());
    }

    @Override
    public void dispatchMessage(Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Exception e) {
            EMSLogger.log(CoreTopic.CONCURRENCY, "Exception occurred in handler: %s", e);
        }
    }
}
