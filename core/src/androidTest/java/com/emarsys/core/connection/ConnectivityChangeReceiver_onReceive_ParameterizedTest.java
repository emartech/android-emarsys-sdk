package com.emarsys.core.connection;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog.ConnectivityChangeReceiver;
import com.emarsys.core.fake.FakeConnectionChangeListener;
import com.emarsys.test.util.ConnectionTestUtils;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ConnectivityChangeReceiver_onReceive_ParameterizedTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Parameterized.Parameter
    public boolean isConnected;

    @Parameterized.Parameter(1)
    public int connectionType;

    @Parameterized.Parameter(2)
    public ConnectionState connectionState;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false, ConnectivityManager.TYPE_WIFI, ConnectionState.DISCONNECTED},
                {false, ConnectivityManager.TYPE_MOBILE, ConnectionState.DISCONNECTED},
                {false, ConnectivityManager.TYPE_MOBILE_DUN, ConnectionState.DISCONNECTED},
                {false, ConnectivityManager.TYPE_BLUETOOTH, ConnectionState.DISCONNECTED},
                {false, ConnectivityManager.TYPE_ETHERNET, ConnectionState.DISCONNECTED},
                {false, ConnectivityManager.TYPE_VPN, ConnectionState.DISCONNECTED},
                {false, ConnectivityManager.TYPE_WIMAX, ConnectionState.DISCONNECTED},

                {true, ConnectivityManager.TYPE_WIFI, ConnectionState.CONNECTED},
                {true, ConnectivityManager.TYPE_MOBILE, ConnectionState.CONNECTED_MOBILE_DATA},
                {true, ConnectivityManager.TYPE_MOBILE_DUN, ConnectionState.CONNECTED_MOBILE_DATA},
                {true, ConnectivityManager.TYPE_BLUETOOTH, ConnectionState.CONNECTED},
                {true, ConnectivityManager.TYPE_ETHERNET, ConnectionState.CONNECTED},
                {true, ConnectivityManager.TYPE_VPN, ConnectionState.CONNECTED},
                {true, ConnectivityManager.TYPE_WIMAX, ConnectionState.CONNECTED}
        });
    }

    @Test
    public void testOnReceive() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FakeConnectionChangeListener listener = new FakeConnectionChangeListener(latch);
        Context contextMock = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(isConnected, connectionType);

        Handler handler = new CoreSdkHandlerProvider().provideHandler();
        ConnectivityChangeReceiver receiver = new ConnectionWatchDog(contextMock, handler).new ConnectivityChangeReceiver(listener);

        Context nullContext = null;
        Intent nullIntent = null;
        receiver.onReceive(nullContext, nullIntent);

        latch.await();

        assertEquals(connectionState, listener.connectionState);
        assertEquals(isConnected, listener.isConnected);
    }
}
