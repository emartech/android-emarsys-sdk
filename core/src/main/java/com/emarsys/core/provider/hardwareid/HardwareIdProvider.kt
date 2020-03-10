package com.emarsys.core.provider.hardwareid

import android.content.Context
import android.provider.Settings
import com.emarsys.core.Mockable
import com.emarsys.core.storage.Storage

@Mockable
class HardwareIdProvider(private val context: Context, private val storage: Storage<String>) {


    fun provideHardwareId(): String {
        var hardwareId: String? = storage.get();
        if (hardwareId == null) {
            hardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)!!
            storage.set(hardwareId)
        }
        return hardwareId
    }
}