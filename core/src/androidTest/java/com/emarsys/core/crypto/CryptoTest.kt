package com.emarsys.core.crypto

import android.util.Base64
import io.kotlintest.shouldBe
import org.junit.Test
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class CryptoTest {
    private companion object {
        const val PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE13w3svUdF8sESRXJwMcyJuFh6jfGc/uKICzwo0cHaHcvZxlt+RrqIBMAo8khDG2yf5SUcDfhK3Ycf+eopgFAkQ=="
    }

    @Test
    fun testVerify_success() {
        val crypto = Crypto(createPublicKey())
        val result = crypto.verify("testData".toByteArray(),
                byteArrayOf(48, 69, 2, 32, 99, 63, -5, 1, -18, 100, 51, 95, -92, -17, -40, -54, -15, -126, -99, -61, -60, -23, -86, -101, 35, 28, -13, 101, 22, -32, 49, 117, 46, 56, -53, 70, 2, 33, 0, -14, -33, 38, -71, -16, -124, -71, 22, -111, 87, 20, 126, 104, 92, -7, 32, 21, -103, -43, -34, 78, 97, -87, -35, -87, 99, -100, 65, 82, -93, 71, 35)
        )

        result shouldBe true
    }

    @Test
    fun testVerify_failed() {
        val crypto = Crypto(createPublicKey())
        val result = crypto.verify("testData2".toByteArray(),
                byteArrayOf(48, 69, 2, 32, 99, 63, -5, 1, -18, 100, 51, 95, -92, -17, -40, -54, -15, -126, -99, -61, -60, -23, -86, -101, 35, 28, -13, 101, 22, -32, 49, 117, 46, 56, -53, 70, 2, 33, 0, -14, -33, 38, -71, -16, -124, -71, 22, -111, 87, 20, 126, 104, 92, -7, 32, 21, -103, -43, -34, 78, 97, -87, -35, -87, 99, -100, 65, 82, -93, 71, 35)
        )
        result shouldBe false
    }

    private fun createPublicKey(): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(
                Base64.decode(PUBLIC_KEY, 0)
        )
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(publicKeySpec)
    }
}