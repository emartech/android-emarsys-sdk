package com.emarsys.core.provider.hardwareid

import com.emarsys.core.contentresolver.hardwareid.HardwareIdContentResolver
import com.emarsys.core.crypto.HardwareIdentificationCrypto
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.HardwareIdentification
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*


class HardwareIdProviderTest{

    companion object {
        private const val HARDWARE_ID = "hw_value"
        internal const val SHARED_HARDWARE_ID = "shared_hw_value"
        internal const val ENCRYPTED_HARDWARE_ID = "encrypted_shared_hardware_id"
        internal const val SALT = "testSalt"
        internal const val IV = "testIv"
        private val HARDWARE = HardwareIdentification(HARDWARE_ID)
        private val SHARED_HARDWARE = HardwareIdentification(SHARED_HARDWARE_ID)
        private val ENCRYPTED_HARDWARE = HardwareIdentification(HARDWARE_ID, "encrypted_hardware_id", "testSalt", "testIv")
        private val ENCRYPTED_SHARED_HARDWARE = HardwareIdentification(SHARED_HARDWARE_ID, "encrypted_shared_hardware_id", "testSalt", "testIv")
    }

    private lateinit var mockHardwareIdentificationCrypto: HardwareIdentificationCrypto
    private lateinit var mockHardwareIdContentResolver: HardwareIdContentResolver
    private lateinit var mockStorage: Storage<String?>
    private lateinit var hardwareIdProvider: HardwareIdProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockRepository: Repository<HardwareIdentification?, SqlSpecification>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
     fun setUp() {
        mockStorage = mock()
        mockUUIDProvider = mock {
            on { provideId() } doReturn HARDWARE_ID
        }

        mockRepository = mock()
        mockHardwareIdentificationCrypto = mock {
            on { encrypt(HARDWARE) } doReturn ENCRYPTED_HARDWARE
            on { encrypt(SHARED_HARDWARE) } doReturn ENCRYPTED_SHARED_HARDWARE
            on { decrypt(ENCRYPTED_HARDWARE_ID, SALT, IV) } doReturn SHARED_HARDWARE_ID
        }
        mockHardwareIdContentResolver = mock()

        hardwareIdProvider = HardwareIdProvider(mockUUIDProvider, mockRepository, mockStorage, mockHardwareIdContentResolver, mockHardwareIdentificationCrypto)
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromRepository_ifExists() {
        whenever(mockRepository.query(any())).thenReturn(listOf(HARDWARE))
        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).query(any())
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromStorage_andStoreInRepository_ifNotInRepository_andSecretNotSet() {
        whenever(mockStorage.get()).thenReturn(HARDWARE_ID)
        whenever(mockHardwareIdentificationCrypto.encrypt(HARDWARE)).thenReturn(HARDWARE)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockRepository).query(any())
        verify(mockHardwareIdentificationCrypto).encrypt(HARDWARE)
        verify(mockStorage).get()
        verify(mockRepository).add(HARDWARE)
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContentResolver_andStoreInRepository_ifNotInRepositoryNorStorage() {
        whenever(mockHardwareIdContentResolver.resolveHardwareId()).thenReturn(SHARED_HARDWARE_ID)

        val result = hardwareIdProvider.provideHardwareId()
        verify(mockRepository).add(HardwareIdentification(SHARED_HARDWARE_ID, ENCRYPTED_HARDWARE_ID, SALT, IV))
        verify(mockHardwareIdContentResolver).resolveHardwareId()
        verifyNoInteractions(mockUUIDProvider)

        result shouldBe SHARED_HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGenerateHardwareId_andStoreInRepository_ifNotInRepositoryNorStorageNorContentResolver() {
        whenever(mockHardwareIdContentResolver.resolveHardwareId()).thenReturn(null)
        whenever(mockHardwareIdentificationCrypto.encrypt(HARDWARE)).thenReturn(HARDWARE)
        whenever(mockUUIDProvider.provideId()).thenReturn(HARDWARE_ID)

        val result = hardwareIdProvider.provideHardwareId()
        verify(mockRepository).add(HARDWARE)
        verify(mockUUIDProvider).provideId()

        result shouldBe HARDWARE_ID
    }
}