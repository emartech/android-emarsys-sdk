package com.emarsys.core.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.emarsys.core.Mockable
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.session.Session

@Mockable
class AppLifecycleObserver(private val session: Session,
                           private val coreSdkHandler: CoreSdkHandler) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        coreSdkHandler.post {
            session.startSession()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        coreSdkHandler.post {
            session.endSession()
        }
    }
}