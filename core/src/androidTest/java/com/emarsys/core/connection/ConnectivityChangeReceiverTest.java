package com.emarsys.core.connection;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.fake.FakeConnectionChangeListener;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class ConnectivityChangeReceiverTest {

    ConnectionWatchDog.ConnectivityChangeReceiver receiver;
    ConnectionChangeListener listener;
    Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        listener = mock(ConnectionChangeListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_listenerCannotBeNull() {
        receiver = new ConnectionWatchDog(context, mock(Handler.class)).new ConnectivityChangeReceiver(null);
    }

    @Test
    public void testOnReceive_listenerShouldCall_onCoreSDKThread() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        FakeConnectionChangeListener fakeListener = new FakeConnectionChangeListener(latch);

        CoreSdkHandlerProvider provider = new CoreSdkHandlerProvider();
        Handler sdkHandler = provider.provideHandler();
        String expectedName = sdkHandler.getLooper().getThread().getName();

        receiver = new ConnectionWatchDog(context, sdkHandler).new ConnectivityChangeReceiver(fakeListener);
        receiver.onReceive(context, mock(Intent.class));

        latch.await();

        assertEquals(1, fakeListener.onConnectionChangedCount);
        assertEquals(expectedName, fakeListener.threadName);
        sdkHandler.getLooper().quit();
    }
}