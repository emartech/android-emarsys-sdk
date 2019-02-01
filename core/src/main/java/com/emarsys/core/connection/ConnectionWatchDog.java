package com.emarsys.core.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.emarsys.core.util.Assert;

public class ConnectionWatchDog {

    private IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    private BroadcastReceiver receiver;
    private Handler coreSdkHandler;

    ConnectivityManager connectivityManager;

    Context context;

    protected ConnectionWatchDog() {
    }

    public ConnectionWatchDog(Context context, Handler coreSdkHandler) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");

        this.context = context.getApplicationContext();
        this.coreSdkHandler = coreSdkHandler;
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public ConnectionState getConnectionState() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork == null || !activeNetwork.isConnected()) {
            return ConnectionState.DISCONNECTED;
        } else if (ConnectivityManager.TYPE_MOBILE == activeNetwork.getType()
                || ConnectivityManager.TYPE_MOBILE_DUN == activeNetwork.getType()) {
            return ConnectionState.CONNECTED_MOBILE_DATA;
        }

        return ConnectionState.CONNECTED;
    }

    public boolean isConnected() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void registerReceiver(ConnectionChangeListener connectionChangeListener) {
        if (receiver == null) {
            receiver = new ConnectivityChangeReceiver(connectionChangeListener);
            context.registerReceiver(receiver, intentFilter);
        } else {
            throw new IllegalStateException("Register receiver cannot be called multiple times!");
        }
    }

    class ConnectivityChangeReceiver extends BroadcastReceiver {

        ConnectionChangeListener connectionChangeListener;

        public ConnectivityChangeReceiver(ConnectionChangeListener connectionChangeListener) {
            Assert.notNull(connectionChangeListener, "ConnectionChangeListener must not be null!");

            this.connectionChangeListener = connectionChangeListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectionState connectionState = getConnectionState();
            final boolean isConnected = isConnected();

            coreSdkHandler.post(new Runnable() {
                @Override
                public void run() {
                    connectionChangeListener.onConnectionChanged(connectionState, isConnected);
                }
            });
        }

    }

}
