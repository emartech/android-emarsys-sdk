package com.emarsys.core.connection;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.test.filters.SdkSuppress;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.fake.FakeConnectionChangeListener;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.concurrent.CountDownLatch;

public class ConnectivityChangeReceiverTest {

    ConnectionWatchDog.ConnectivityChangeReceiver receiver;
    ConnectionChangeListener listener;
    Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        listener = mock(ConnectionChangeListener.class);
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    public void testOnReceive_listenerShouldCall_onCoreSDKThread() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        FakeConnectionChangeListener fakeListener = new FakeConnectionChangeListener(latch);

        CoreSdkHandlerProvider provider = new CoreSdkHandlerProvider();
        CoreSdkHandler sdkHandler = provider.provideHandler();
        String expectedName = sdkHandler.getLooper().getThread().getName();

        receiver = new ConnectionWatchDog(context, sdkHandler).new ConnectivityChangeReceiver(fakeListener);
        receiver.onReceive(context, mock(Intent.class));

        latch.await();

        assertEquals(1, fakeListener.onConnectionChangedCount);
        assertEquals(expectedName, fakeListener.threadName);
        sdkHandler.getLooper().quit();
    }
}