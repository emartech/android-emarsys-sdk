package com.emarsys.core.connection;


import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.test.filters.SdkSuppress;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog.ConnectivityChangeReceiver;
import com.emarsys.core.fake.FakeConnectionChangeListener;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.testUtil.ConnectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

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
                {false, NetworkCapabilities.TRANSPORT_WIFI, ConnectionState.DISCONNECTED},
                {false, NetworkCapabilities.TRANSPORT_CELLULAR, ConnectionState.DISCONNECTED},
                {false, NetworkCapabilities.TRANSPORT_ETHERNET, ConnectionState.DISCONNECTED},
                {false, NetworkCapabilities.TRANSPORT_VPN, ConnectionState.DISCONNECTED},

                {true, NetworkCapabilities.TRANSPORT_WIFI, ConnectionState.CONNECTED},
                {true, NetworkCapabilities.TRANSPORT_CELLULAR, ConnectionState.CONNECTED_MOBILE_DATA},
                {true, NetworkCapabilities.TRANSPORT_ETHERNET, ConnectionState.CONNECTED},
                {true, NetworkCapabilities.TRANSPORT_VPN, ConnectionState.CONNECTED}
        });
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.N_MR1)
    public void testOnReceive() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FakeConnectionChangeListener listener = new FakeConnectionChangeListener(latch);
        Context contextMock = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(isConnected, connectionType);

        CoreSdkHandler handler = new CoreSdkHandlerProvider().provideHandler();
        ConnectivityChangeReceiver receiver = new ConnectionWatchDog(contextMock, handler).new ConnectivityChangeReceiver(listener);

        Context nullContext = null;
        Intent nullIntent = null;
        receiver.onReceive(nullContext, nullIntent);

        latch.await();

        assertEquals(connectionState, listener.connectionState);
        assertEquals(isConnected, listener.isConnected);
    }
}
