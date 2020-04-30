package com.emarsys.core.crypto

import android.util.Base64
import com.emarsys.core.Mockable
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import java.lang.Exception
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
        return try {
            val sig = Signature.getInstance(ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(messageBytes)
            sig.verify(Base64.decode(signatureBytes, Base64.DEFAULT))
        } catch (exception: Exception) {
            Logger.error(CrashLog(exception))
            false
        }
    }
}