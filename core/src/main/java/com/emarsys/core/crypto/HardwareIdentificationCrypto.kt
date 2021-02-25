package com.emarsys.core.crypto

import com.emarsys.core.Mockable
import com.emarsys.core.device.HardwareIdentification

@Mockable
class HardwareIdentificationCrypto(private var secret: String?,
                                   private val crypto: Crypto) {

    fun encrypt(hardwareIdentification: HardwareIdentification): HardwareIdentification {
        var result = hardwareIdentification
        if (secret != null) {
            val encryptedHardwareIdentification = crypto.encrypt(hardwareIdentification.hardwareId, secret!!)
            result = result.copy(
                    encryptedHardwareId = encryptedHardwareIdentification["encryptedValue"],
                    salt = encryptedHardwareIdentification["salt"],
                    iv = encryptedHardwareIdentification["iv"]
            )
        }
        return result
    }

    fun decrypt(encryptedHardwareId: String, salt: String, iv: String): String? {
        return secret?.let {
            crypto.decrypt(encryptedHardwareId, it, salt, iv)
        }
    }
}