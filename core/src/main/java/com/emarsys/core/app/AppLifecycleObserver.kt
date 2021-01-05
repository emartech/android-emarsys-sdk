package com.emarsys.core.app

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.emarsys.core.Mockable
import com.emarsys.core.session.Session

@Mockable
class AppLifecycleObserver(private val session: Session,
                           private val coreSdkHandler: Handler) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        coreSdkHandler.post {
            session.startSession()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        coreSdkHandler.post {
            session.endSession()
        }
    }
}