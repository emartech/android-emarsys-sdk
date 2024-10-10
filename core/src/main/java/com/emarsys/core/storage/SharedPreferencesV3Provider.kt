package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.crypto.SharedPreferenceCrypto

class SharedPreferencesV3Provider(
    context: Context,
    fileName: String,
    oldSharedPreferences: SharedPreferences,
    crypto: SharedPreferenceCrypto,
    migration: EncryptedSharedPreferencesToSharedPreferencesMigration
) {

    private var sharedPreferences: SharedPreferences =
        EmarsysEncryptedSharedPreferencesV3(context, fileName, crypto)

    init {
        migration.migrate(oldSharedPreferences, sharedPreferences)
    }

    fun provide(): SharedPreferences {
        return sharedPreferences
    }
}