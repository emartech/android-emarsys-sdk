package com.emarsys.core.provider.hardwareid

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.device.Hardware
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage

@Mockable
class HardwareIdProvider(private val context: Context,
                         private val uuidProvider: UUIDProvider,
                         private val repository: Repository<Hardware?, SqlSpecification>,
                         private val storage: Storage<String?>,
                         private val sharedPackageNames: List<String>?) {

    fun provideHardwareId(): String {
        return repository.query(Everything()).firstOrNull()?.hardwareId
                ?: storage.get()?.saveToRepository()
                ?: getHardwareIdFromContentResolver()?.saveToRepository()
                ?: uuidProvider.provideId().saveToRepository()
    }

    private fun String.saveToRepository(): String {
        repository.add(Hardware(this))
        return this
    }

    private fun getHardwareIdFromContentResolver(): String? {
        var hardwareId: String? = null
        var index = 0
        if (sharedPackageNames != null) {
            while (hardwareId == null && index < sharedPackageNames.size) {
                val cursor = context.contentResolver.query(DatabaseContract.getHardwareIdProviderUri(sharedPackageNames[index]),
                        arrayOf(DatabaseContract.HARDWARE_COLUMN_NAME_HARDWARE_ID), null, null, null)
                if (cursor?.moveToFirst() != null) {
                    hardwareId = cursor.getString(cursor.getColumnIndex(DatabaseContract.HARDWARE_COLUMN_NAME_HARDWARE_ID))
                    cursor.close()
                }
                index++
            }
        }
        return hardwareId
    }
}