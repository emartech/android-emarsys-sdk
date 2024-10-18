import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import com.emarsys.core.crypto.SharedPreferenceCrypto
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SharedPreferenceCryptoTest : AnnotationSpec() {
    private companion object {
        const val encryptedBase64 = "Base64EncryptedBase64IV123123"

    }

    private lateinit var sharedPreferenceCrypto: SharedPreferenceCrypto
    private lateinit var mockKeyStore: KeyStore
    private lateinit var mockKeyGenerator: KeyGenerator
    private lateinit var mockSecretKey: SecretKey
    private lateinit var mockCipher: Cipher

    @BeforeEach
    fun setup() {
        mockkStatic(KeyStore::class)
        mockkStatic(KeyGenerator::class)
        mockkStatic(Cipher::class)
        mockkStatic(Base64::class)

        mockKeyStore = mockk()
        mockKeyGenerator = mockk()
        mockSecretKey = mockk()
        mockCipher = mockk()

        every { KeyStore.getInstance(any()) } returns mockKeyStore
        every { KeyGenerator.getInstance(any(), any<String>()) } returns mockKeyGenerator
        every { Cipher.getInstance(any()) } returns mockCipher

        sharedPreferenceCrypto = SharedPreferenceCrypto()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testGetOrCreateSecretKey_KeyExists() {
        every { mockKeyStore.load(null) } just Runs
        every { mockKeyStore.containsAlias(any()) } returns true
        every { mockKeyStore.getKey(any(), null) } returns mockSecretKey

        val result = sharedPreferenceCrypto.getOrCreateSecretKey()

        result shouldBe mockSecretKey
        verify { mockKeyStore.getKey(any(), null) }
    }

    @Test
    fun testGetOrCreateSecretKey_KeyDoesNotExist() {
        every { mockKeyStore.load(null) } just Runs
        every { mockKeyStore.containsAlias(any()) } returns false
        every { mockKeyGenerator.init(any<KeyGenParameterSpec>()) } just Runs
        every { mockKeyGenerator.generateKey() } returns mockSecretKey

        val result = sharedPreferenceCrypto.getOrCreateSecretKey()

        result shouldBe mockSecretKey
        verify { mockKeyGenerator.generateKey() }
    }

    @Test
    fun testEncrypt_Success() {
        val value = "test_value"
        val encryptedBytes = byteArrayOf(1, 2, 3, 4)
        val iv = byteArrayOf(5, 6, 7, 8)

        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) } just Runs
        every { mockCipher.doFinal(any<ByteArray>()) } returns encryptedBytes
        every { mockCipher.iv } returns iv
        every { Base64.encodeToString(any(), Base64.DEFAULT) } returns "encodedString"

        val result = sharedPreferenceCrypto.encrypt(value, mockSecretKey)

        result shouldNotBe value
        result shouldBe "encodedStringencodedString"
    }

    @Test
    fun testEncrypt_Exception() {
        val value = "test_value"

        every {
            mockCipher.init(
                Cipher.ENCRYPT_MODE,
                mockSecretKey
            )
        } throws GeneralSecurityException("Encryption failed")

        val result = sharedPreferenceCrypto.encrypt(value, mockSecretKey)

        result shouldBe value
    }

    @Test
    fun testDecrypt_Success() {
        val ivBytes = byteArrayOf(1, 2, 3, 4)
        val encryptedBytes = byteArrayOf(5, 6, 7, 8)
        val decryptedBytes = "decrypted".toByteArray()

        every { Base64.decode(any<String>(), Base64.DEFAULT) } returnsMany listOf(
            ivBytes,
            encryptedBytes
        )
        every {
            mockCipher.init(
                Cipher.DECRYPT_MODE,
                mockSecretKey,
                any<GCMParameterSpec>()
            )
        } just Runs
        every { mockCipher.doFinal(encryptedBytes) } returns decryptedBytes

        val result = sharedPreferenceCrypto.decrypt(encryptedBase64, mockSecretKey)

        result shouldBe "decrypted"
    }

    @Test
    fun testDecrypt_Exception() {
        val IVValue = "Base64EncryptedBase64IV123"
        val decryptedBytes = encryptedBase64.toByteArray()
        every {
            mockCipher.init(any(), mockSecretKey, any<GCMParameterSpec>())
        } just Runs
        every {
            mockCipher.doFinal(any())
        } returns decryptedBytes
        every {
            Base64.decode(
                IVValue,
                Base64.DEFAULT
            )
        } throws GeneralSecurityException("Decryption failed")

        val result = sharedPreferenceCrypto.decrypt(encryptedBase64, mockSecretKey)

        result shouldBe encryptedBase64
    }
}