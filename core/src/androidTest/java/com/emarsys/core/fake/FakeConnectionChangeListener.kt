package com.emarsys.core.fake

import com.emarsys.core.connection.ConnectionChangeListener
import com.emarsys.core.connection.ConnectionState
import java.util.concurrent.CountDownLatch

class FakeConnectionChangeListener(var latch: CountDownLatch) : ConnectionChangeListener {

    var onConnectionChangedCount = 0
    var threadName: String? = null
    var connectionState: ConnectionState? = null
    var isConnected = false

    override fun onConnectionChanged(connectionState: ConnectionState?, isConnected: Boolean) {
        this.connectionState = connectionState
        this.isConnected = isConnected
        onConnectionChangedCount++
        threadName = Thread.currentThread().name
        latch.countDown()
    }
}
