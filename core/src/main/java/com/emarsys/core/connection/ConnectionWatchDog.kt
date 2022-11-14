package com.emarsys.core.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.AndroidVersionUtils

@Mockable
class ConnectionWatchDog(
    inputContext: Context,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) : ConnectivityManager.NetworkCallback() {
    private companion object {
        var receiver: BroadcastReceiver? = null
    }

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
            return try {
                val activeNetwork =
                    connectivityManager.activeNetwork ?: return ConnectionState.DISCONNECTED
                val networkCapabilities =
                    connectivityManager.getNetworkCapabilities(activeNetwork)
                        ?: return ConnectionState.DISCONNECTED
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionState.CONNECTED
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionState.CONNECTED
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionState.CONNECTED
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionState.CONNECTED_MOBILE_DATA

                    else -> ConnectionState.DISCONNECTED
                }
            } catch (ignored: Exception) {
                ConnectionState.DISCONNECTED
            }
        }
    val isConnected: Boolean
        get() {
            return try {
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
            } catch (ignored: Exception) {
                false
            }
        }


    fun registerReceiver(connectionChangeListener: ConnectionChangeListener) {
        try {
            if (AndroidVersionUtils.isOreoOrAbove) {
                this.connectionChangeListener = connectionChangeListener
                connectivityManager.registerNetworkCallback(
                    networkRequest,
                    this,
                    concurrentHandlerHolder.coreHandler.handler
                )
            } else {
                if (receiver != null) {
                    receiver =
                        ConnectivityChangeReceiver(
                            connectionChangeListener,
                            this,
                            concurrentHandlerHolder
                        )
                    context.registerReceiver(receiver, intentFilter)
                }
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val connectionState = connectionState
        val isConnected = isConnected
        connectionChangeListener.onConnectionChanged(connectionState, isConnected)
    }
}