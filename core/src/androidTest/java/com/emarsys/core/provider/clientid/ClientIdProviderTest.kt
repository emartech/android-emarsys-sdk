package com.emarsys.core.provider.clientid

import com.emarsys.core.contentresolver.clientid.ClientIdContentResolver
import com.emarsys.core.crypto.ClientIdentificationCrypto
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.ClientIdentification
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ClientIdProviderTest  {

    companion object {
        private const val CLIENT_ID = "hw_value"
        internal const val SHARED_CLIENT_ID = "shared_hw_value"
        internal const val ENCRYPTED_CLIENT_ID = "encrypted_shared_hardware_id"
        internal const val SALT = "testSalt"
        internal const val IV = "testIv"
        private val CLIENT = ClientIdentification(CLIENT_ID)
        private val SHARED_CLIENT = ClientIdentification(SHARED_CLIENT_ID)
        private val ENCRYPTED_CLIENT =
            ClientIdentification(CLIENT_ID, "encrypted_hardware_id", "testSalt", "testIv")
        private val ENCRYPTED_SHARED_HARDWARE = ClientIdentification(
            SHARED_CLIENT_ID,
            "encrypted_shared_hardware_id",
            "testSalt",
            "testIv"
        )
    }

    private lateinit var mockClientIdentificationCrypto: ClientIdentificationCrypto
    private lateinit var mockClientIdContentResolver: ClientIdContentResolver
    private lateinit var mockStorage: Storage<String?>
    private lateinit var clientIdProvider: ClientIdProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockRepository: Repository<ClientIdentification?, SqlSpecification>


    @Before
    fun setUp() {
        mockStorage = mock()
        mockUUIDProvider = mock {
            on { provideId() } doReturn CLIENT_ID
        }

        mockRepository = mock()
        mockClientIdentificationCrypto = mock {
            on { encrypt(CLIENT) } doReturn ENCRYPTED_CLIENT
            on { encrypt(SHARED_CLIENT) } doReturn ENCRYPTED_SHARED_HARDWARE
            on { decrypt(ENCRYPTED_CLIENT_ID, SALT, IV) } doReturn SHARED_CLIENT_ID
        }
        mockClientIdContentResolver = mock()

        clientIdProvider = ClientIdProvider(
            mockUUIDProvider,
            mockRepository,
            mockStorage,
            mockClientIdContentResolver,
            mockClientIdentificationCrypto
        )
    }

    @Test
    fun testProvideClientId_shouldGetClientId_fromRepository_ifExists() {
        whenever(mockRepository.query(any())).thenReturn(listOf(CLIENT))
        val result = clientIdProvider.provideClientId()

        verify(mockRepository).query(any())
        result shouldBe CLIENT_ID
    }

    @Test
    fun testProvideClientId_shouldGetClientId_fromStorage_andStoreInRepository_ifNotInRepository_andSecretNotSet() {
        whenever(mockStorage.get()).thenReturn(CLIENT_ID)
        whenever(mockClientIdentificationCrypto.encrypt(CLIENT)).thenReturn(CLIENT)

        runBlocking {
            val result = clientIdProvider.provideClientId()
            verify(mockRepository).query(any())
            verify(mockClientIdentificationCrypto).encrypt(CLIENT)
            verify(mockStorage).get()
            verify(mockRepository).add(CLIENT)
            result shouldBe CLIENT_ID
        }
    }

    @Test
    fun testProvideClientId_shouldGetClientId_fromContentResolver_andStoreInRepository_ifNotInRepositoryNorStorage() {
        whenever(mockClientIdContentResolver.resolveClientId()).thenReturn(SHARED_CLIENT_ID)

        runBlocking {
            val result = clientIdProvider.provideClientId()
            verify(mockRepository).add(
                ClientIdentification(
                    SHARED_CLIENT_ID,
                    ENCRYPTED_CLIENT_ID,
                    SALT,
                    IV
                )
            )
            verify(mockClientIdContentResolver).resolveClientId()
            verifyNoInteractions(mockUUIDProvider)

            result shouldBe SHARED_CLIENT_ID
        }
    }

    @Test
    fun testProvideClientId_shouldGenerateClientId_andStoreInRepository_ifNotInRepositoryNorStorageNorContentResolver() {
        whenever(mockClientIdContentResolver.resolveClientId()).thenReturn(null)
        whenever(mockClientIdentificationCrypto.encrypt(CLIENT)).thenReturn(CLIENT)
        whenever(mockUUIDProvider.provideId()).thenReturn(CLIENT_ID)
        runBlocking {
            val result = clientIdProvider.provideClientId()
            verify(mockRepository).add(CLIENT)
            verify(mockUUIDProvider).provideId()

            result shouldBe CLIENT_ID
        }
    }
}