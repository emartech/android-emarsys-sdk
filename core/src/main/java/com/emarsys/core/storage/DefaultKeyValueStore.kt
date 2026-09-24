package com.emarsys.core.storage

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit

@SuppressLint("ApplySharedPref")
class DefaultKeyValueStore(private val prefs: SharedPreferences) : KeyValueStore {

    override fun putString(key: String, value: String) {
        prefs.edit(commit = true) { putString(key, value) }
    }

    override fun putInt(key: String, value: Int) {
        prefs.edit(commit = true) { putInt(key, value) }
    }

    override fun putLong(key: String, value: Long) {
        prefs.edit(commit = true) { putLong(key, value) }
    }

    override fun putFloat(key: String, value: Float) {
        prefs.edit(commit = true) { putFloat(key, value) }
    }

    override fun putDouble(key: String, value: Double) {
        prefs.edit(commit = true) { putLong(key, java.lang.Double.doubleToRawLongBits(value)) }
    }

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit(commit = true) { putBoolean(key, value) }
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun getInt(key: String): Int = prefs.getInt(key, 0)

    override fun getLong(key: String): Long = prefs.getLong(key, 0)

    override fun getFloat(key: String): Float = prefs.getFloat(key, 0f)

    override fun getDouble(key: String): Double =
        java.lang.Double.longBitsToDouble(prefs.getLong(key, 0))

    override fun getBoolean(key: String): Boolean = prefs.getBoolean(key, false)

    override fun remove(key: String) {
        prefs.edit(commit = true) { remove(key) }
    }

    override fun clear() {
        prefs.edit(commit = true) { clear() }
    }

    override val size: Int get() = prefs.all.size

    override val isEmpty: Boolean get() = prefs.all.isEmpty()
}
