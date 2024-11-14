package com.emarsys.core.provider.clientid

import com.emarsys.core.Mockable
import com.emarsys.core.contentresolver.clientid.ClientIdContentResolver
import com.emarsys.core.crypto.ClientIdentificationCrypto
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.device.ClientIdentification
import com.emarsys.core.device.FilterByClientId
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog

@Mockable
class ClientIdProvider(
    private val uuidProvider: UUIDProvider,
    private val repository: Repository<ClientIdentification?, SqlSpecification>,
    private val clientIdStorage: Storage<String?>,
    private val clientIdContentResolver: ClientIdContentResolver,
    private val clientIdentificationCrypto: ClientIdentificationCrypto
) {

    fun provideClientId(): String {
        val client: ClientIdentification? = try {
            repository.query(Everything()).firstOrNull()
        } catch (error: Exception) {
            val status = mutableMapOf<String, Any>()
            status["message"] = error.message ?: ""
            status["stackTrace"] = error.stackTrace.map { it.toString() }
            Logger.error(
                StatusLog(
                    this::class.java,
                    "provideClientId",
                    parameters = null,
                    status = status
                )
            )
            null
        }
        return if (client != null) {
            if (client.encryptedClientId == null) {
                clientIdentificationCrypto.encrypt(client).also {
                    repository.update(it, FilterByClientId(it.clientId))
                }
                client.clientId
            } else {
                client.clientId
            }
        } else {
            getClientIdentification().also {
                repository.add(it)
            }.clientId
        }
    }

    private fun getClientIdentification(): ClientIdentification {
        return (clientIdStorage.get() ?: clientIdContentResolver.resolveClientId()
        ?: generateNewClientId()).asEncryptedClientIdentification()
    }

    private fun generateNewClientId(): String {
        return uuidProvider.provideId()
    }

    private fun String.asEncryptedClientIdentification(): ClientIdentification {
        return clientIdentificationCrypto.encrypt(ClientIdentification(this))
    }
}