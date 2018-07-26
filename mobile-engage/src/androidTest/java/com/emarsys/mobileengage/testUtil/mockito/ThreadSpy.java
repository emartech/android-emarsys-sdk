package com.emarsys.mobileengage.testUtil.mockito;

import android.os.Looper;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class ThreadSpy<T> implements Answer<T> {

    private Thread thread;
    private CountDownLatch latch;
    private T result;

    public ThreadSpy() {
        this(null);
    }

    public ThreadSpy(T result) {
        latch = new CountDownLatch(1);
        this.result = result;
    }

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        thread = Thread.currentThread();
        latch.countDown();
        return result;
    }

    public Thread getThread() throws InterruptedException {
        latch.await();
        return thread;
    }

    public void verifyCalledOnMainThread() throws InterruptedException {
        Thread expected = Looper.getMainLooper().getThread();
        Thread result = getThread();
        assertEquals(expected, result);
    }

    public void verifyCalledOnCoreSdkThread() throws InterruptedException {
        Thread result = getThread();
        assertThat(result.getName(), startsWith("CoreSDKHandlerThread"));
    }
}
