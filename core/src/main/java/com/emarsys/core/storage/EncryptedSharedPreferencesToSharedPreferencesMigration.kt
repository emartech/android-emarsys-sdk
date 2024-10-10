package com.emarsys.core.storage

import android.content.SharedPreferences

class EncryptedSharedPreferencesToSharedPreferencesMigration {

    fun migrate(
        oldSharedPreferences: SharedPreferences,
        newSharedPreferences: SharedPreferences
    ) {
        try {
            val encryptedData = oldSharedPreferences.all
            val editor = newSharedPreferences.edit()
            for ((key, value) in encryptedData) {
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Long -> editor.putLong(key, value)
                    is Set<*> -> editor.putStringSet(key, value as Set<String>)
                }
            }
            editor.apply()
            oldSharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}