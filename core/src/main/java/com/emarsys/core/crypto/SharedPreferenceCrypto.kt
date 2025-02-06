package com.emarsys.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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

    fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
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

        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    fun encrypt(value: String, secretKey: SecretKey): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(value.toByteArray())
            val iv = cipher.iv
            val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
            val encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT)
            "$ivBase64$encryptedBase64"
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            value
        }
    }

    fun decrypt(value: String, secretKey: SecretKey): String? {
        return try {
            val ivBytes = Base64.decode(value.substring(0, 16), Base64.DEFAULT)
            val encryptedBytes = Base64.decode(value.substring(16), Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))
            val decrypted = cipher.doFinal(encryptedBytes)
            String(decrypted)
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}