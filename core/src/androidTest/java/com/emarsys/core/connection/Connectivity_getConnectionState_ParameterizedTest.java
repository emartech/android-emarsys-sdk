package com.emarsys.core.connection;


import static junit.framework.Assert.assertEquals;

import android.net.NetworkCapabilities;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.testUtil.ConnectionTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

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
    public void testGetConnectionState_connectionWatchDog() {
        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(InstrumentationRegistry.getTargetContext(), new CoreSdkHandlerProvider().provideHandler());
        ReflectionTestUtils.setInstanceField(connectionWatchDog, "connectivityManager", ConnectionTestUtils.getConnectivityManagerMock(isConnected, connectionType));

        assertEquals(connectionState, connectionWatchDog.getConnectionState());
    }

}
