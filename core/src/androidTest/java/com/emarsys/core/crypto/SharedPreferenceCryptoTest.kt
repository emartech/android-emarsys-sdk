package com.emarsys.core.crypto

import android.security.keystore.KeyProperties
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class SharedPreferenceCryptoTest {
    private lateinit var keyStore: KeyStore

    @Before
    fun setup() {
        keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.deleteEntry("emarsys_sdk_key_shared_pref_key_v3")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun init_shouldGenerateKey_ifNotPresent_inKeyStore() {
        mockkStatic(KeyGenerator::class)

        SharedPreferenceCrypto()

        verify { KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES) }
    }

    @Test
    fun init_shouldNotGenerateKey_ifPresent_inKeyStore() {
        mockkStatic(KeyGenerator::class)

        SharedPreferenceCrypto()
        SharedPreferenceCrypto()

        verify(exactly = 1) { KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES) }
    }

    @Test
    fun encrypt_decrypt_shouldWork() {
        val testValue = "testValue"

        val testCrypto = SharedPreferenceCrypto()
        val encrypted = testCrypto.encrypt(testValue)

        testCrypto.decrypt(encrypted) shouldBe testValue
    }

    @Test
    fun encrypt_shouldGenerateNewSecretKey_andRetryEncrypting_once_andReturnEncryptedValue_ifSucceeds() {
        val testValue = "testValue"
        mockkStatic(KeyGenerator::class)
        mockkStatic(Cipher::class)
        every { Cipher.getInstance("AES/GCM/NoPadding") } throws GeneralSecurityException("Test exception") andThenAnswer { callOriginal() }

        val testCrypto = SharedPreferenceCrypto()

        val result = testCrypto.encrypt(testValue)

        verify(exactly = 2) { KeyGenerator.getInstance(any()) }

        result shouldNotBe testValue
    }

    @Test
    fun encrypt_shouldGenerateNewSecretKey_andRetryEncrypting_once_andReturnInitialValue_ifFails() {
        val testValue = "testValue"
        mockkStatic(KeyGenerator::class)
        mockkStatic(Cipher::class)
        every { Cipher.getInstance("AES/GCM/NoPadding") } throws GeneralSecurityException("Test exception")

        val testCrypto = SharedPreferenceCrypto()

        val result = testCrypto.encrypt(testValue)

        verify(exactly = 2) { KeyGenerator.getInstance(any()) }
        result shouldBe testValue
    }

    @Test
    fun decrypt_shouldReturn_null_andGenerateNewSecretKey_ifGeneralSecurityExceptionHappens() {
        val testValue = "dGVzdFZhbHVlU2hvdWxkQmVTaXh0ZWVuQ2hhcnNMb25n"
        mockkStatic(KeyGenerator::class)
        mockkStatic(Cipher::class)
        every { Cipher.getInstance("AES/GCM/NoPadding") } throws GeneralSecurityException("Test exception")

        val testCrypto = SharedPreferenceCrypto()

        testCrypto.decrypt(testValue) shouldBe null

        verify { KeyGenerator.getInstance(any()) }
    }

    @Test
    fun decrypt_shouldReturn_encryptedValue_ifIllegalArgumentException_withBase64ErrorHappens() {
        val testValue = "testValueShouldBeSixteenCharsLong"

        val testCrypto = SharedPreferenceCrypto()
        testCrypto.decrypt(testValue) shouldBe testValue
    }

    @Test
    fun decrypt_shouldReturn_null_ifIllegalArgumentExceptionHappens() {
        val testValue = "dGVzdFZhbHVlU2hvdWxkQmVTaXh0ZWVuQ2hhcnNMb25n"
        mockkStatic(Cipher::class)
        every { Cipher.getInstance("AES/GCM/NoPadding") } throws IllegalArgumentException("Test exception")

        val testCrypto = SharedPreferenceCrypto()
        testCrypto.decrypt(testValue) shouldBe null
    }

    @Test
    fun decrypt_shouldReturn_null_ifExceptionHappens() {
        val testValue = "testValue"

        val testCrypto = SharedPreferenceCrypto()
        testCrypto.decrypt(testValue) shouldBe null
    }
}