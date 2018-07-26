package com.emarsys.core.connection;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityUtils {

    public static ConnectionState getConnectionState(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (!activeNetwork.isConnected()) {
            return ConnectionState.DISCONNECTED;
        } else if (ConnectivityManager.TYPE_MOBILE == activeNetwork.getType()
                || ConnectivityManager.TYPE_MOBILE_DUN == activeNetwork.getType()) {
            return ConnectionState.CONNECTED_MOBILE_DATA;
        }

        return ConnectionState.CONNECTED;
    }

    public static boolean isConnected(ConnectivityManager connectivityManager) {
        return connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
