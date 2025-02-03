package com.emarsys.core.crypto

import com.emarsys.core.Mockable
import com.emarsys.core.device.ClientIdentification

@Mockable
class ClientIdentificationCrypto(
    private var secret: String?,
    private val crypto: Crypto
) {

    fun encrypt(clientIdentification: ClientIdentification): ClientIdentification {
        var result = clientIdentification
        if (secret != null) {
            val encryptedClientIdentification =
                crypto.encrypt(clientIdentification.clientId, secret!!)
            result = result.copy(
                encryptedClientId = encryptedClientIdentification["encryptedValue"],
                salt = encryptedClientIdentification["salt"],
                iv = encryptedClientIdentification["iv"]
            )
        }
        return result
    }

    fun decrypt(encryptedClientId: String, salt: String, iv: String): String? {
        return secret?.let {
            crypto.decrypt(encryptedClientId, it, salt, iv)
        }
    }
}