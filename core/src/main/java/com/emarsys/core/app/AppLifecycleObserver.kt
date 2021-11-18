package com.emarsys.core.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.emarsys.core.Mockable
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.session.Session
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog

@Mockable
class AppLifecycleObserver(private val session: Session,
                           private val coreSdkHandler: CoreSdkHandler) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        coreSdkHandler.post {
            session.startSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        coreSdkHandler.post {
            session.endSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
            }
        }
    }
}