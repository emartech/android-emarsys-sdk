package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.crypto.SharedPreferenceCrypto

class SharedPreferencesV3Provider(
    context: Context,
    fileName: String,
    crypto: SharedPreferenceCrypto
) {
    private val sharedPreferences: SharedPreferences =
        EmarsysEncryptedSharedPreferencesV3(context, fileName, crypto)

    fun provide(): SharedPreferences = sharedPreferences
}
