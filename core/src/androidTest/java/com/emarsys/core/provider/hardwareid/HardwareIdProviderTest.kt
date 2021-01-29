package com.emarsys.core.provider.hardwareid

import android.database.Cursor
import android.net.Uri
import android.test.ProviderTestCase2
import android.test.mock.MockContentProvider
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.FilterByHardwareId
import com.emarsys.core.device.HardwareIdentification
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


class HardwareIdProviderTest : ProviderTestCase2<FakeContentProvider>(FakeContentProvider::class.java, "com.emarsys.test") {

    companion object {
        private const val HARDWARE_ID = "hw_value"
        internal const val SHARED_HARDWARE_ID = "shared_hw_value"
        private const val SECRET = "testSecret"
        private val HARDWARE = HardwareIdentification(HARDWARE_ID)
        private val ENCRYPTED_HARDWARE = HardwareIdentification(HARDWARE_ID, "encrypted_hardware_id", "testSalt", "testIv")
        private val ENCRYPTED_SHARED_HARDWARE = HardwareIdentification(SHARED_HARDWARE_ID, "encrypted_shared_hardware_id", "testSalt", "testIv")
    }

    private lateinit var mockCrypto: Crypto
    private lateinit var mockStorage: Storage<String?>
    private lateinit var hardwareIdProvider: HardwareIdProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockRepository: Repository<HardwareIdentification?, SqlSpecification>
    private val sharedPackageNames = listOf("com.emarsys.test")

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    override fun setUp() {
        super.setUp()
        mockStorage = mock()
        mockUUIDProvider = mock {
            on { provideId() } doReturn HARDWARE_ID
        }

        mockRepository = mock()
        mockCrypto = mock {
            on { encrypt(HARDWARE_ID, SECRET) } doReturn mapOf(
                    "encryptedValue" to "encrypted_hardware_id",
                    "salt" to "testSalt",
                    "iv" to "testIv"
            )
            on { encrypt(SHARED_HARDWARE_ID, SECRET) } doReturn mapOf(
                    "encryptedValue" to "encrypted_shared_hardware_id",
                    "salt" to "testSalt",
                    "iv" to "testIv"
            )
        }

        hardwareIdProvider = HardwareIdProvider(this.mockContext, null, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, sharedPackageNames)
    }

    fun testProvideHardwareId_shouldGetHardwareId_fromRepository_ifExists() {
        whenever(mockRepository.query(any())).thenReturn(listOf(HARDWARE))
        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).query(any())
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromStorage_andStoreInRepository_ifNotInRepository() {
        whenever(mockStorage.get()).thenReturn(HARDWARE_ID)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).query(any())
        verify(mockStorage).get()
        verify(mockRepository).add(HARDWARE)
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromStorage_andEncryptWithSecret_andStoreInRepository_ifNotInRepository() {
        hardwareIdProvider = HardwareIdProvider(this.mockContext, SECRET, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, sharedPackageNames)
        whenever(mockStorage.get()).thenReturn(HARDWARE_ID)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).query(any())
        verify(mockStorage).get()
        verify(mockCrypto).encrypt(HARDWARE_ID, SECRET)
        verify(mockRepository).add(ENCRYPTED_HARDWARE)
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContentResolver_andStoreInRepository_ifNotInRepositoryNorStorage() {
        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).add(HardwareIdentification(SHARED_HARDWARE_ID))
        result shouldBe SHARED_HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContentResolver_lookingInSharedPackages_andStoreInRepository_ifNotInRepositoryNorStorage() {
        FakeContentProvider.numberOfInvocation = 0
        hardwareIdProvider = HardwareIdProvider(this.mockContext, null, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, listOf("test.package", "com.emarsys.test", "com.emarsys.test"))

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).add(HardwareIdentification(SHARED_HARDWARE_ID))
        FakeContentProvider.numberOfInvocation shouldBe 1
        result shouldBe SHARED_HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContentResolver_lookingInSharedPackages_andEncryptWithSecret_andStoreInRepository_ifNotInRepositoryNorStorage() {
        FakeContentProvider.numberOfInvocation = 0
        hardwareIdProvider = HardwareIdProvider(this.mockContext, SECRET, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, listOf("test.package", "com.emarsys.test", "com.emarsys.test"))

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockCrypto).encrypt(SHARED_HARDWARE_ID, SECRET)
        verify(mockRepository).add(ENCRYPTED_SHARED_HARDWARE)
        FakeContentProvider.numberOfInvocation shouldBe 1
        result shouldBe SHARED_HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGenerateHardwareId_andStoreInRepository_ifNotInRepositoryNorStorageNorContentResolver() {
        val context = InstrumentationRegistry.getTargetContext()
        hardwareIdProvider = HardwareIdProvider(context, null, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, sharedPackageNames)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).add(HARDWARE)
        verify(mockUUIDProvider).provideId()
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGenerateHardwareId_andEncryptWithSecret_andStoreInRepository_ifNotInRepositoryNorStorageNorContentResolver() {
        val context = InstrumentationRegistry.getTargetContext()
        hardwareIdProvider = HardwareIdProvider(context, SECRET, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, sharedPackageNames)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockCrypto).encrypt(HARDWARE_ID, SECRET)
        verify(mockRepository).add(ENCRYPTED_HARDWARE)
        verify(mockUUIDProvider).provideId()
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldCallCryptoEncrypt_whenThereIsASecret_butThereIsNoEncryptedHardwareId() {
        hardwareIdProvider = HardwareIdProvider(this.mockContext, SECRET, mockCrypto, mockUUIDProvider, mockRepository, mockStorage, sharedPackageNames)

        whenever(mockRepository.query(any())).thenReturn(listOf(HARDWARE))

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockCrypto).encrypt(HARDWARE_ID, SECRET)
        verify(mockRepository).update(ENCRYPTED_HARDWARE,FilterByHardwareId(HARDWARE_ID))
        result shouldBe HARDWARE_ID
    }
}

open class FakeContentProvider : MockContentProvider() {
    companion object {
        var numberOfInvocation = 0
    }

    private var cursor: Cursor = mock {
        on { moveToFirst() } doReturn true
        on { getColumnIndex(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_HARDWARE_ID) } doReturn 0
        on { getString(0) } doReturn HardwareIdProviderTest.SHARED_HARDWARE_ID
    }

    override fun query(uri: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        numberOfInvocation++
        return cursor
    }
}