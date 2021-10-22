package com.emarsys.core.connection

interface ConnectionChangeListener {
    fun onConnectionChanged(connectionState: ConnectionState?, isConnected: Boolean)
}