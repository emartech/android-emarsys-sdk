package com.emarsys.mobileengage.notification

import android.app.Application.ActivityLifecycleCallbacks
import com.emarsys.core.Mockable
import java.util.concurrent.CountDownLatch

@Mockable
class LaunchActivityCommandLifecycleCallbacksFactory {
    fun create(latch: CountDownLatch): ActivityLifecycleCallbacks {
        return LaunchActivityCommandLifecycleCallbacks(latch)
    }
}