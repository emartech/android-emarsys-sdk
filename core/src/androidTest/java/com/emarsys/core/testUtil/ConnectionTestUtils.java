package com.emarsys.core.testUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionTestUtils {

    public static ConnectivityManager getConnectivityManagerMock(boolean isConnected, int connectionType) {
        ConnectivityManager managerMock = mock(ConnectivityManager.class);
        NetworkInfo networkInfoMock = mock(NetworkInfo.class);
        when(managerMock.getActiveNetworkInfo()).thenReturn(networkInfoMock);
        when(networkInfoMock.isConnected()).thenReturn(isConnected);
        when(networkInfoMock.getType()).thenReturn(connectionType);
        return managerMock;
    }

    public static Context getContextMock_withAppContext_withConnectivityManager(boolean isConnected, int connectionType) {
        Context contextMock = mock(Context.class);
        Context applicationContextMock = mock(Context.class);
        ConnectivityManager managerMock = ConnectionTestUtils.getConnectivityManagerMock(isConnected, connectionType);

        when(contextMock.getApplicationContext()).thenReturn(applicationContextMock);
        when(applicationContextMock.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(managerMock);
        return contextMock;
    }

    public static Context getContextMock_withConnectivityManager(boolean isConnected, int connectionType) {
        Context contextMock = mock(Context.class);
        ConnectivityManager managerMock = ConnectionTestUtils.getConnectivityManagerMock(isConnected, connectionType);

        when(contextMock.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(managerMock);
        return contextMock;
    }

    public static void checkConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            throw new RuntimeException("Device is not connected to the Internet!");
        }
    }
}
