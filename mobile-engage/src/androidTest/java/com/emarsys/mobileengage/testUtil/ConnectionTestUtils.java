package com.emarsys.mobileengage.testUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionTestUtils {
    public static void checkConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            throw new RuntimeException("Device is not connected to the Internet!");
        }
    }
}
