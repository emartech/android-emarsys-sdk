package com.emarsys.core.contentresolver.hardwareid

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.crypto.HardwareIdentificationCrypto
import com.emarsys.core.database.DatabaseContract

@Mockable
class HardwareIdContentResolver(private val context: Context,
                                private val crypto: HardwareIdentificationCrypto,
                                private val sharedPackageNames: List<String>?) {

    fun resolveHardwareId(): String? {
        var index = 0
        var sharedHardwareId: String? = null
        var encryptedHardwareId: String? = null
        if (sharedPackageNames != null) {
            while (encryptedHardwareId == null && index < sharedPackageNames.size) {
                val cursor = context.contentResolver.query(DatabaseContract.getHardwareIdProviderUri(sharedPackageNames[index]),
                        arrayOf(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID,
                                DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT,
                                DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV),
                        null, null, null)
                if (cursor?.moveToFirst() != null) {
                    encryptedHardwareId = cursor.getString(cursor.getColumnIndex(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID))
                    val salt = cursor.getString(cursor.getColumnIndex(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT))
                    val iv = cursor.getString(cursor.getColumnIndex(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV))
                    sharedHardwareId = crypto.decrypt(encryptedHardwareId, salt, iv)
                    cursor.close()
                }
                index++
            }
        }
        return sharedHardwareId
    }
}