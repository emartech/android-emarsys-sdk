package com.emarsys.core.concurrency;

import android.os.HandlerThread;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class CoreSdkHandlerTest {

    CoreSdkHandler handler;
    HandlerThread handlerThread;
    Runnable failingRunnable;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        String threadName = "test";
        handlerThread = new HandlerThread(threadName);
        handlerThread.start();

        handler = new CoreSdkHandler(handlerThread);

        failingRunnable = new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("error");
            }
        };
    }

    @After
    public void tearDown() {
        handlerThread.quit();
    }

    @Test
    public void testConstructor_innerLooper_isInitialized() {
        Assert.assertNotNull(handler.getLooper());
        Assert.assertEquals(handlerThread.getName(), handler.getLooper().getThread().getName());
    }

    @Test
    public void testDispatchMessage_shouldBeResilient_toExceptions() throws InterruptedException {
        handler.post(failingRunnable);
        handler.post(failingRunnable);

        Thread.sleep(100);

        Assert.assertTrue(handler.getLooper().getThread().isAlive());
    }

}