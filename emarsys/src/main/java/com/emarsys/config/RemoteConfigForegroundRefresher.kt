package com.emarsys.config

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.emarsys.core.Mockable
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.handler.ConcurrentHandlerHolder

@Mockable
class RemoteConfigForegroundRefresher(
    private val configInternal: DefaultConfigInternal,
    private val connectionWatchDog: ConnectionWatchDog,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (configInternal.hasFetchedThisSession || !connectionWatchDog.isConnected) {
            return
        }
        concurrentHandlerHolder.coreHandler.post {
            configInternal.refreshRemoteConfig(null)
        }
    }
}
