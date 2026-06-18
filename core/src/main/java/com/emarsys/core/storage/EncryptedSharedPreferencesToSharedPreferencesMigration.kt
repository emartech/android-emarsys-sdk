package com.emarsys.core.storage

import android.content.SharedPreferences
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog

class EncryptedSharedPreferencesToSharedPreferencesMigration {

    fun migrate(
        oldSharedPreferences: SharedPreferences,
        newSharedPreferences: SharedPreferences
    ) {
        try {
            val encryptedData = oldSharedPreferences.all
            val editor = newSharedPreferences.edit()
            for ((key, value) in encryptedData) {
                try {
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Int -> editor.putInt(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Float -> editor.putFloat(key, value)
                        is Long -> editor.putLong(key, value)
                        is Set<*> -> editor.putStringSet(key, value as Set<String>)
                    }
                } catch (e: Exception) {
                    Logger.error(
                        StatusLog(
                            EncryptedSharedPreferencesToSharedPreferencesMigration::class.java,
                            "migrate#perKey",
                            mapOf(
                                "key" to key,
                                "value" to value,
                                "exception" to e.message
                            )
                        )
                    )
                    throw e
                }
            }
            editor.apply()
            oldSharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Logger.error(
                StatusLog(
                    EncryptedSharedPreferencesToSharedPreferencesMigration::class.java,
                    "migrate",
                    mapOf("exception" to e.message)
                )
            )
            try {
                oldSharedPreferences.edit().clear().apply()
            } catch (clearException: Exception) {
                Logger.error(
                    StatusLog(
                        EncryptedSharedPreferencesToSharedPreferencesMigration::class.java,
                        "migrate#clearOld",
                        mapOf("exception" to clearException.message)
                    )
                )
            }
            try {
                newSharedPreferences.edit().clear().apply()
            } catch (clearException: Exception) {
                Logger.error(
                    StatusLog(
                        EncryptedSharedPreferencesToSharedPreferencesMigration::class.java,
                        "migrate#clearNew",
                        mapOf("exception" to clearException.message)
                    )
                )
            }
        }
    }
}
