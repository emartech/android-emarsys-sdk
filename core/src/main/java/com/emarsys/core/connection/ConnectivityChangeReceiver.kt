package com.emarsys.core.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.core.handler.CoreSdkHandler

class ConnectivityChangeReceiver(
    private val connectionChangeListener: ConnectionChangeListener,
    private val connectionWatchDog: ConnectionWatchDog,
    private val coreSdkHandler: CoreSdkHandler
) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        coreSdkHandler.post {
            val connectionState: ConnectionState = connectionWatchDog.connectionState
            val isConnected: Boolean = connectionWatchDog.isConnected
            connectionChangeListener.onConnectionChanged(connectionState, isConnected)
        }
    }
}