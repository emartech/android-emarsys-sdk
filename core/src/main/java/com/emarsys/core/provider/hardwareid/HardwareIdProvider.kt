package com.emarsys.core.provider.hardwareid

import android.content.Context
import android.provider.Settings
import com.emarsys.core.Mockable
import com.emarsys.core.storage.Storage
import com.google.firebase.iid.FirebaseInstanceId

@Mockable
class HardwareIdProvider(private val context: Context, private val firebaseInstanceId: FirebaseInstanceId, private val storage: Storage<String>) {
    fun provideHardwareId(): String {
        return storage.get()
                ?: return if (firebaseInstanceId.id.isNullOrBlank()) {
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                } else {
                    firebaseInstanceId.id
                }.also {
                    storage.set(it)
                }
    }
}