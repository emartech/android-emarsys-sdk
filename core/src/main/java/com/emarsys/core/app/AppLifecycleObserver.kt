package com.emarsys.core.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.session.Session
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog

@Mockable
class AppLifecycleObserver(
    private val session: Session,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        concurrentHandlerHolder.coreHandler.post {
            session.startSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        concurrentHandlerHolder.coreHandler.post {
            session.endSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
            }
        }
    }
}