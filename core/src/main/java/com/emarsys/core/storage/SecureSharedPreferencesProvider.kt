package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog

class SecureSharedPreferencesProvider(context: Context, fileName: String, oldSharedPreferences: SharedPreferences) {

    private var sharedPreferences: SharedPreferences

    init {
        sharedPreferences =
                try {
                    EncryptedSharedPreferences.create(
                            context,
                            fileName,
                            createMasterKey(context),
                            PrefKeyEncryptionScheme.AES256_SIV,
                            PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (e: Exception) {
                    EmarsysSecureSharedPreferences.create(
                            fileName,
                            context
                    )
                }

        if (oldSharedPreferences.all.isNotEmpty()) {
            oldSharedPreferences.all.asSequence().forEach { entry ->
                with(sharedPreferences.edit()) {
                    when (entry.value) {
                        is Int -> putInt(entry.key, entry.value as Int)
                        is Boolean -> putBoolean(entry.key, entry.value as Boolean)
                        is Float -> putFloat(entry.key, entry.value as Float)
                        is Long -> putLong(entry.key, entry.value as Long)
                        is String -> putString(entry.key, entry.value as String)
                        else -> Logger.error(StatusLog(this::class.java, "sharedPreferencesProvider#migrationToSecure", mapOf(entry.key to entry.value)))
                    }
                    commit()
                }
            }
            oldSharedPreferences.edit().clear().commit()
        }
    }

    private fun createMasterKey(context: Context): MasterKey {
        val spec = KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                .build()
        return MasterKey.Builder(context)
                .setKeyGenParameterSpec(spec)
                .build()
    }

    fun provide(): SharedPreferences {
        return sharedPreferences
    }
}