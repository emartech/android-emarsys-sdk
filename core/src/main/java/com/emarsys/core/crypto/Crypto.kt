package com.emarsys.core.crypto

import com.emarsys.core.Mockable
import java.security.PublicKey
import java.security.Signature

@Mockable
class Crypto(private val publicKey: PublicKey) {
    companion object {
        const val ALGORITHM = "SHA256withECDSA"
    }

    fun verify(
            messageBytes: ByteArray,
            signatureBytes: ByteArray
    ): Boolean {
        val sig = Signature.getInstance(ALGORITHM)
        sig.initVerify(publicKey)
        sig.update(messageBytes)
        return sig.verify(signatureBytes)
    }
}