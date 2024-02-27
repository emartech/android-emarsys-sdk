package com.emarsys.core.crypto

import com.emarsys.core.device.HardwareIdentification
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HardwareIdentificationCryptoTest : AnnotationSpec() {

    companion object {
        private const val SECRET = "SECRET"
        private const val HARDWARE_ID = "testHardwareId"
        private const val ENCRYPTED_HARDWARE_ID = "testEncryptedHardwareId"
        private const val SALT = "testSalt"
        private const val IV = "testIv"
        private val HARDWARE = HardwareIdentification(HARDWARE_ID, null, null, null)
        private val ENCRYPTED_HARDWARE =
            HardwareIdentification(HARDWARE_ID, ENCRYPTED_HARDWARE_ID, SALT, IV)
    }

    private lateinit var hardwareIdentificationCryptoWithSecret: HardwareIdentificationCrypto
    private lateinit var hardwareIdentificationCryptoWithoutSecret: HardwareIdentificationCrypto
    private lateinit var mockCrypto: Crypto

    @Before
    fun setUp() {
        mockCrypto = mock()

        hardwareIdentificationCryptoWithSecret = HardwareIdentificationCrypto(SECRET, mockCrypto)
        hardwareIdentificationCryptoWithoutSecret = HardwareIdentificationCrypto(null, mockCrypto)
    }

    @Test
    fun testEncrypt_doNothing_whenSecretIsMissing() {
        val result = hardwareIdentificationCryptoWithoutSecret.encrypt(HARDWARE)

        result shouldBe HARDWARE
    }

    @Test
    fun testEncrypt_shouldEncrypt_whenSecretIsGiven() {
        whenever(mockCrypto.encrypt(HARDWARE_ID, SECRET)).thenReturn(mapOf(
                "encryptedValue" to ENCRYPTED_HARDWARE_ID,
                "salt" to SALT,
                "iv" to IV))
        val result = hardwareIdentificationCryptoWithSecret.encrypt(HARDWARE)

        result shouldBe ENCRYPTED_HARDWARE
    }

    @Test
    fun testDecrypt_shouldGiveBack_hardwareId_fromEncryptedHardware(){
        whenever(mockCrypto.decrypt(ENCRYPTED_HARDWARE_ID, SECRET, SALT, IV)).thenReturn(HARDWARE_ID)

        val result = hardwareIdentificationCryptoWithSecret.decrypt(ENCRYPTED_HARDWARE_ID, SALT, IV)

        result shouldBe HARDWARE_ID
    }
}