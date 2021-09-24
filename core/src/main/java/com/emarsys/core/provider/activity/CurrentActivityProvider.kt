package com.emarsys.core.provider.activity

import android.app.Activity
import com.emarsys.core.Mockable
import com.emarsys.core.provider.Property
import java.lang.ref.WeakReference

@Mockable
class CurrentActivityProvider(
    private var activityWeakReference: WeakReference<Activity?> = WeakReference(null)
) : Property<Activity?> {

    override fun get(): Activity? {
        return activityWeakReference.get()
    }

    override fun set(value: Activity?) {
        activityWeakReference = WeakReference(value)
    }
}