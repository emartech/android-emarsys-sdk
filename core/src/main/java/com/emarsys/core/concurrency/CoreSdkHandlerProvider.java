package com.emarsys.core.concurrency;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.UUID;

public class CoreSdkHandlerProvider {

    public Handler provideHandler() {
        HandlerThread handlerThread = new HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString());
        handlerThread.start();
        return new CoreSdkHandler(handlerThread);
    }

}