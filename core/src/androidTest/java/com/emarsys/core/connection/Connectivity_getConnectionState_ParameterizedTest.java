package com.emarsys.core.connection;


import android.net.ConnectivityManager;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.test.util.ConnectionTestUtils;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

@RunWith(Parameterized.class)
public class Connectivity_getConnectionState_ParameterizedTest {

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
                {true, ConnectivityManager.TYPE_WIMAX, ConnectionState.CONNECTED},
        });
    }

    @Test
    public void testGetConnectionState_connectivityUtils() {
        ConnectivityManager connectivityManager = ConnectionTestUtils.getConnectivityManagerMock(isConnected, connectionType);

        assertEquals(connectionState, ConnectivityUtils.getConnectionState(connectivityManager));
    }

    @Test
    public void testGetConnectionState_connectionWatchDog() {
        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(InstrumentationRegistry.getContext(), new CoreSdkHandlerProvider().provideHandler());
        connectionWatchDog.connectivityManager = ConnectionTestUtils.getConnectivityManagerMock(isConnected, connectionType);

        assertEquals(connectionState, connectionWatchDog.getConnectionState());
    }

}
