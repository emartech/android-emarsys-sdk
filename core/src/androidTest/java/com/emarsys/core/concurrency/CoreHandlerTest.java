package com.emarsys.core.concurrency;

import android.os.HandlerThread;
import com.emarsys.testUtil.TimeoutUtils;
import org.junit.*;
import org.junit.rules.TestRule;

import java.util.concurrent.CountDownLatch;

public class CoreHandlerTest {

    CoreHandler handler;
    HandlerThread handlerThread;
    Runnable failingRunnable;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        String threadName = "test";
        handlerThread = new HandlerThread(threadName);
        handlerThread.start();

        handler = new CoreHandler(handlerThread);

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
        final CountDownLatch latch = new CountDownLatch(1);

        handler.post(failingRunnable);
        handler.post(failingRunnable);

        handler.post(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        latch.await();

        Assert.assertTrue(handler.getLooper().getThread().isAlive());
    }

}