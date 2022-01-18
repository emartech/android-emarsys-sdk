package com.emarsys.core.provider.hardwareid

import com.emarsys.core.Mockable
import com.emarsys.core.contentresolver.hardwareid.HardwareIdContentResolver
import com.emarsys.core.crypto.HardwareIdentificationCrypto
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.device.FilterByHardwareId
import com.emarsys.core.device.HardwareIdentification
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import kotlinx.coroutines.runBlocking

@Mockable
class HardwareIdProvider(
    private val uuidProvider: UUIDProvider,
    private val repository: Repository<HardwareIdentification?, SqlSpecification>,
    private val hwIdStorage: Storage<String?>,
    private val hardwareIdContentResolver: HardwareIdContentResolver,
    private val hardwareIdentificationCrypto: HardwareIdentificationCrypto
) {

    fun provideHardwareId(): String {
        val hardware = repository.query(Everything()).firstOrNull()
        return if (hardware != null) {
            if (hardware.encryptedHardwareId == null) {
                hardwareIdentificationCrypto.encrypt(hardware).also {
                    runBlocking {
                        repository.update(it, FilterByHardwareId(it.hardwareId))
                    }
                }
                hardware.hardwareId
            } else {
                hardware.hardwareId
            }
        } else {
            getHardwareIdentification().also {
                runBlocking {
                    repository.add(
                        it
                    )
                }
            }.hardwareId
        }
    }

    private fun getHardwareIdentification(): HardwareIdentification {
        return (hwIdStorage.get()
            ?: hardwareIdContentResolver.resolveHardwareId()
            ?: generateNewHardwareId()
                ).asEncryptedHardwareIdentification()
    }

    private fun generateNewHardwareId(): String {
        return uuidProvider.provideId()
    }

    private fun String.asEncryptedHardwareIdentification(): HardwareIdentification {
        return hardwareIdentificationCrypto.encrypt(HardwareIdentification(this))
    }
}