package com.emarsys.core.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

public class ConnectionWatchDog {

    IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    BroadcastReceiver receiver;

    ConnectivityManager connectivityManager;

    Context context;
    Handler coreSdkHandler;

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
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        EMSLogger.log(CoreTopic.CONNECTIVITY, "Connected to the network: %s", isConnected);
        return isConnected;
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
            EMSLogger.log(CoreTopic.CONNECTIVITY, "ConnectionState: %s, isConnected: %s", connectionState, isConnected);

            coreSdkHandler.post(new Runnable() {
                @Override
                public void run() {
                    connectionChangeListener.onConnectionChanged(connectionState, isConnected);
                }
            });
        }

    }

}
