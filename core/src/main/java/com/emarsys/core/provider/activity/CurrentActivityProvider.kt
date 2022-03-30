package com.emarsys.core.provider.activity

import android.annotation.SuppressLint
import android.app.Activity
import com.emarsys.core.Mockable
import com.emarsys.core.provider.Property
import java.lang.ref.WeakReference
import java.lang.reflect.Field

@Mockable
class CurrentActivityProvider(
    private var activityWeakReference: WeakReference<Activity?> = WeakReference(null)
) : Property<Activity?> {

    override fun get(): Activity? {
        if (activityWeakReference.get() == null) {
            activityWeakReference = WeakReference(getActivityWithReflection())
        }
        return activityWeakReference.get()
    }

    override fun set(value: Activity?) {
        activityWeakReference = WeakReference(value)
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun getActivityWithReflection(): Activity? {
        var result: Activity? = null

        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val activitiesField: Field = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            val activities = (activitiesField.get(activityThread) as Map<Any, Any>?) ?: emptyMap()
            activities.values.forEach { activityRecord ->
                val activityRecordClass: Class<*> = activityRecord.javaClass
                val pausedField: Field = activityRecordClass.getDeclaredField("paused")
                pausedField.isAccessible = true
                if (!pausedField.getBoolean(activityRecord)) {
                    val activityField: Field = activityRecordClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    result = activityField.get(activityRecord) as Activity?
                }
            }
        } catch (ignored: Exception) {
        }

        return result
    }
}