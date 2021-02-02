package com.emarsys.core.provider.hardwareid

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.device.FilterByHardwareId
import com.emarsys.core.device.HardwareIdentification
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage

@Mockable
class HardwareIdProvider(private val context: Context,
                         private val secret: String?,
                         private val crypto: Crypto,
                         private val uuidProvider: UUIDProvider,
                         private val repository: Repository<HardwareIdentification?, SqlSpecification>,
                         private val storage: Storage<String?>,
                         private val sharedPackageNames: List<String>?) {

    fun provideHardwareId(): String {
        val hardware = repository.query(Everything()).firstOrNull()
        return if (hardware != null) {
            if (secret != null && hardware.encryptedHardwareId == null) {
                hardware.encrypt(secret).also {
                    repository.update(it, FilterByHardwareId(it.hardwareId))
                }
                hardware.hardwareId
            } else {
                hardware.hardwareId
            }
        } else {
            generateHardwareId().hardwareId
        }
    }

    private fun HardwareIdentification.encrypt(secret: String?): HardwareIdentification {
        return if (secret != null) {
            val encryptedHardware = crypto.encrypt(hardwareId, secret)
            copy(
                    encryptedHardwareId = encryptedHardware["encryptedValue"],
                    salt = encryptedHardware["salt"],
                    iv = encryptedHardware["iv"])
        } else {
            this
        }
    }

    private fun generateHardwareId() = storage.get()?.toHardware()?.encrypt(secret)?.saveToRepository()
            ?: getHardwareIdFromContentResolver()?.toHardware()?.encrypt(secret)?.saveToRepository()
            ?: uuidProvider.provideId().toHardware().encrypt(secret).saveToRepository()

    private fun HardwareIdentification.saveToRepository(): HardwareIdentification {
        repository.add(this)
        return this
    }

    private fun String.toHardware() = HardwareIdentification(this)

    private fun getHardwareIdFromContentResolver(): String? {
        var index = 0
        var sharedHardwareId: String? = null
        var encryptedHardwareId: String? = null
        if (sharedPackageNames != null && secret != null) {
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
                    sharedHardwareId = crypto.decrypt(encryptedHardwareId, secret, salt, iv)
                    cursor.close()
                }
                index++
            }
        }
        return sharedHardwareId
    }
}