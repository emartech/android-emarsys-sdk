package com.emarsys.core.crypto

import android.util.Base64
import com.emarsys.core.Mockable
import com.google.android.gms.common.util.Hex
import java.security.PublicKey
import java.security.Signature

@Mockable
class Crypto(private val publicKey: PublicKey) {
    companion object {
        const val ALGORITHM = "SHA256withECDSA"
    }

    fun verify(
            messageBytes: ByteArray,
            signatureBytes: String
    ): Boolean {
        val sig = Signature.getInstance(ALGORITHM)
        sig.initVerify(publicKey)
        sig.update(messageBytes)
        return sig.verify(Base64.decode(signatureBytes, Base64.DEFAULT))
    }
}