package com.emarsys.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SharedPreferenceCrypto {
    companion object {
        private const val KEYSTORE_ALIAS = "emarsys_sdk_key_shared_pref_key_v3"
    }

    private var secretKey: SecretKey = getOrCreateSecretKey()

    fun encrypt(value: String): String {
        return try {
            tryEncrypt(value)
        } catch (exception: GeneralSecurityException) {
            logCryptoError(value, "encrypt", exception)
            secretKey = createSecretKey()
            try {
                tryEncrypt(value)
            } catch (exception: Exception) {
                logCryptoError(value, "encrypt", exception)
                value
            }
        }
    }

    fun decrypt(value: String): String? {
        return try {
            val ivBytes = Base64.decode(value.substring(0, 16), Base64.DEFAULT)
            val encryptedBytes = Base64.decode(value.substring(16), Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))
            val decrypted = cipher.doFinal(encryptedBytes)
            String(decrypted)
        } catch (exception: GeneralSecurityException) {
            logCryptoError(value, "decrypt", exception)
            secretKey = createSecretKey()
            null
        } catch (exception: IllegalArgumentException) {
            logCryptoError(value, "decrypt", exception)
            if (exception.message?.contains("bad base-64") == true) {
                value
            } else {
                null
            }
        } catch (exception: Exception) {
            logCryptoError(value, "decrypt", exception)
            null
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            return createSecretKey()
        }

        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    private fun createSecretKey(): SecretKey {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun tryEncrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(value.toByteArray())
        val iv = cipher.iv
        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT)
        return "$ivBase64$encryptedBase64"
    }

    private fun logCryptoError(value: String, methodName: String, exception: Exception) {
        val logEntry = StatusLog(
            SharedPreferenceCrypto::class.java,
            methodName,
            mapOf(
                "reason" to "Failed to decrypt value: $value",
                "exception" to exception.message
            )
        )
        Logger.debug(logEntry)
    }
}