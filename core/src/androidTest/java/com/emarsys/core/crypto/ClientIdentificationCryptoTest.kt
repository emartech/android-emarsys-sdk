package com.emarsys.core.crypto

import com.emarsys.core.device.ClientIdentification
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ClientIdentificationCryptoTest  {

    companion object {
        private const val SECRET = "SECRET"
        private const val CLIENT_ID = "testClientId"
        private const val ENCRYPTED_CLIENT_ID = "testEncryptedClientId"
        private const val SALT = "testSalt"
        private const val IV = "testIv"
        private val HARDWARE = ClientIdentification(CLIENT_ID, null, null, null)
        private val ENCRYPTED_HARDWARE =
            ClientIdentification(CLIENT_ID, ENCRYPTED_CLIENT_ID, SALT, IV)
    }

    private lateinit var clientIdentificationCryptoWithSecret: ClientIdentificationCrypto
    private lateinit var clientIdentificationCryptoWithoutSecret: ClientIdentificationCrypto
    private lateinit var mockCrypto: Crypto

    @Before
    fun setUp() {
        mockCrypto = mock()

        clientIdentificationCryptoWithSecret = ClientIdentificationCrypto(SECRET, mockCrypto)
        clientIdentificationCryptoWithoutSecret = ClientIdentificationCrypto(null, mockCrypto)
    }

    @Test
    fun testEncrypt_doNothing_whenSecretIsMissing() {
        val result = clientIdentificationCryptoWithoutSecret.encrypt(HARDWARE)

        result shouldBe HARDWARE
    }

    @Test
    fun testEncrypt_shouldEncrypt_whenSecretIsGiven() {
        whenever(mockCrypto.encrypt(CLIENT_ID, SECRET)).thenReturn(
            mapOf(
                "encryptedValue" to ENCRYPTED_CLIENT_ID,
                "salt" to SALT,
                "iv" to IV))
        val result = clientIdentificationCryptoWithSecret.encrypt(HARDWARE)

        result shouldBe ENCRYPTED_HARDWARE
    }

    @Test
    fun testDecrypt_shouldGiveBack_clientId_fromEncryptedHardware() {
        whenever(mockCrypto.decrypt(ENCRYPTED_CLIENT_ID, SECRET, SALT, IV)).thenReturn(CLIENT_ID)

        val result = clientIdentificationCryptoWithSecret.decrypt(ENCRYPTED_CLIENT_ID, SALT, IV)

        result shouldBe CLIENT_ID
    }
}