package com.emarsys.core.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.emarsys.core.Mockable
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.util.AndroidVersionUtils

@Mockable
class ConnectionWatchDog(
    inputContext: Context,
    private val coreSdkHandler: CoreSdkHandler
) : ConnectivityManager.NetworkCallback() {
    private val context = inputContext.applicationContext
    private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkRequest: NetworkRequest =
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

    private lateinit var connectionChangeListener: ConnectionChangeListener

    val connectionState: ConnectionState
        get() {
            val activeNetwork =
                connectivityManager.activeNetwork ?: return ConnectionState.DISCONNECTED
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork)
                    ?: return ConnectionState.DISCONNECTED
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionState.CONNECTED
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionState.CONNECTED
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionState.CONNECTED
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionState.CONNECTED_MOBILE_DATA

                else -> ConnectionState.DISCONNECTED
            }
        }
    val isConnected: Boolean
        get() {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }


    fun registerReceiver(connectionChangeListener: ConnectionChangeListener) {
        if (AndroidVersionUtils.isOreoOrAbove()) {
            this.connectionChangeListener = connectionChangeListener
            connectivityManager.registerNetworkCallback(
                networkRequest,
                this,
                coreSdkHandler.handler
            )
        } else {
            val receiver = ConnectivityChangeReceiver(connectionChangeListener)
            context.registerReceiver(receiver, intentFilter)
        }
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val connectionState = connectionState
        val isConnected = isConnected
        connectionChangeListener.onConnectionChanged(connectionState, isConnected)
    }


    inner class ConnectivityChangeReceiver(private val connectionChangeListener: ConnectionChangeListener) :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            coreSdkHandler.post {
                val connectionState: ConnectionState = connectionState
                val isConnected: Boolean = isConnected
                connectionChangeListener.onConnectionChanged(connectionState, isConnected)
            }
        }

    }
}