package com.emarsys.core.provider.activity

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.emarsys.core.Mockable
import com.emarsys.core.provider.Property
import java.lang.ref.WeakReference

@Mockable
class CurrentActivityProvider(
    private var activityWeakReference: WeakReference<Activity?> = WeakReference(null),
    val fallbackActivityProvider: FallbackActivityProvider
) : Property<Activity?> {

    override fun get(): Activity? {
        if (activityWeakReference.get() == null) {
            activityWeakReference = WeakReference(fallbackActivityProvider.provide())
        }
        return activityWeakReference.get()
    }

    override fun set(value: Activity?) {
        activityWeakReference = WeakReference(value)
    }
}

fun Activity.fragmentManager(): FragmentManager? {
    var fragmentManager: FragmentManager? = null
    if (this is FragmentActivity) {
        fragmentManager = this.supportFragmentManager
    }
    return fragmentManager
}