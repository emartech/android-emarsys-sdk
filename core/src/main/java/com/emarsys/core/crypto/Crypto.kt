package com.emarsys.core.crypto

import android.util.Base64
import com.emarsys.core.Mockable
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Mockable
class Crypto(private val publicKey: PublicKey) {
    companion object {
        const val REMOTE_CONFIG_CRYPTO_ALGORITHM = "SHA256withECDSA"
        private const val CRYPTO_ALGORITHM_BELOW_26 = "AES/GCM/NoPadding"
        private const val CRYPTO_ALGORITHM_ABOVE_26 = "AES_256/GCM/NoPadding"

        fun getAlgorythm(): String {
            return if (AndroidVersionUtils.isBelowOreo()) CRYPTO_ALGORITHM_BELOW_26 else CRYPTO_ALGORITHM_ABOVE_26
        }
        fun getIvSize(): Int {
            return if (AndroidVersionUtils.isBelowOreo()) 16 else 12
        }
        fun getKeyLength(): Int {
            return if (AndroidVersionUtils.isBelowOreo()) 128 else 256
        }
    }

    fun verify(
            messageBytes: ByteArray,
            signatureBytes: String
    ): Boolean {
        return try {
            val sig = Signature.getInstance(REMOTE_CONFIG_CRYPTO_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(messageBytes)
            sig.verify(Base64.decode(signatureBytes, Base64.DEFAULT))
        } catch (exception: Exception) {
            Logger.error(CrashLog(exception))
            false
        }
    }

    fun encrypt(value: String, secret: String): Map<String, String> {
        val cipher = Cipher.getInstance(getAlgorythm())
        val random = SecureRandom()

        val iv = ByteArray(getIvSize())
        random.nextBytes(iv)
        val salt = ByteArray(16)
        random.nextBytes(salt)

        cipher.init(Cipher.ENCRYPT_MODE, generateKey(secret, salt), IvParameterSpec(iv))

        val textBytes = value.toByteArray(Charsets.UTF_8)
        val encryptedText = cipher?.doFinal(textBytes)

        return mapOf(
                "encryptedValue" to Base64.encodeToString(encryptedText, Base64.DEFAULT),
                "salt" to Base64.encodeToString(salt, Base64.DEFAULT),
                "iv" to Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }

    fun decrypt(encrypted: String, secret: String, salt: String, iv: String): String? {
        val cipher = Cipher.getInstance(getAlgorythm())
        val ivBytes = Base64.decode(iv.toByteArray(), Base64.DEFAULT)
        val saltBytes = Base64.decode(salt.toByteArray(), Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, generateKey(secret, saltBytes), IvParameterSpec(ivBytes))
        val decodedText = Base64.decode(encrypted.toByteArray(), Base64.DEFAULT)
        val decryptedText = cipher?.doFinal(decodedText)
        return decryptedText?.toString(Charsets.UTF_8)
    }

    private fun generateKey(password: String, salt: ByteArray): SecretKeySpec? {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keySpec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, getKeyLength())
        val secretKey = factory.generateSecret(keySpec)
        val encoded = secretKey.encoded
        return SecretKeySpec(encoded, "AES")
    }
}