package com.emarsys.core.fake

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory.create
import com.emarsys.core.connection.ConnectionChangeListener
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import java.util.Arrays
import java.util.concurrent.CountDownLatch

class FakeConnectionWatchDog(var latch: CountDownLatch, vararg isConnectedReplies: Boolean?) :
    ConnectionWatchDog(getTargetContext(), create()) {
    var isConnectedReplies: MutableList<Boolean> =
        ArrayList(Arrays.asList(*isConnectedReplies))
    var connectionChangeListener: ConnectionChangeListener? = null

    override val isConnected: Boolean
        get() {
            val result = isConnectedReplies[0]
            if (isConnectedReplies.size > 1) {
                isConnectedReplies.removeAt(0)
            }

            latch.countDown()
            return result
        }

    override fun registerReceiver(connectionChangeListener: ConnectionChangeListener) {
        this.connectionChangeListener = connectionChangeListener
    }
}
