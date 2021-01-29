package com.emarsys.core.crypto

import android.util.Base64
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class CryptoTest {
    private companion object {
        private const val PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
        private const val SECRET = "testSecret"
    }

    @Test
    fun testVerify_success() {
        val crypto = Crypto(createPublicKey())
        val result = crypto.verify("testData".toByteArray(),
                "MEUCIQDb6AxUK2W4IyKJ/P02Y0BNlm2ioP7ytu3dOyumc4hN8gIgEzwKmeCtd6Jn9Neg4Epn+oSkV4wAJNmfAgeeAM0u7Nw="
        )

        result shouldBe true
    }

    @Test
    fun testVerify_failed() {
        val crypto = Crypto(createPublicKey())
        val result = crypto.verify("testData2".toByteArray(),
                "MEUCIQDb6AxUK2W4IyKJ/P02Y0BNlm2ioP7ytu3dOyumc4hN8gIgEzwKmeCtd6Jn9Neg4Epn+oSkV4wAJNmfAgeeAM0u7Nw="
        )
        result shouldBe false
    }

    @Test
    fun testEncrypt_Decrypt() {
        val testText = "TestText"

        val crypto = Crypto(createPublicKey())
        val encrypted = crypto.encrypt(testText, SECRET)
        val encryptedText = encrypted["encryptedValue"]
        val salt = encrypted["salt"]
        val iv = encrypted["iv"]

        encryptedText shouldNotBe testText

        val decryptedText = crypto.decrypt(encryptedText!!, SECRET, salt!!, iv!!)

        decryptedText shouldBe testText
    }

    private fun createPublicKey(): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(
                Base64.decode(PUBLIC_KEY, 0)
        )
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(publicKeySpec)
    }
}