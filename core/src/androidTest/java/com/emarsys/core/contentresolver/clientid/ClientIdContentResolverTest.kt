import android.database.Cursor
import android.net.Uri
import com.emarsys.core.contentresolver.EmarsysContentResolver
import com.emarsys.core.contentresolver.clientid.ClientIdContentResolver
import com.emarsys.core.crypto.ClientIdentificationCrypto
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.device.ClientIdentification
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ClientIdContentResolverTest  {

    companion object {
        private val SHARED_PACKAGE_NAMES =
            listOf("emarsys.test", "com.emarsys.test", "com.android.test")
        private const val ENCRYPTED_ClIENT_ID = "encrypted_shared_hardware_id"
        private const val SALT = "testSalt"
        private const val IV = "testIv"

    }

    private var mockEmarsysContentResolver: EmarsysContentResolver = mockk()
    private lateinit var contentResolver: ClientIdContentResolver
    private lateinit var mockClientIdentificationCrypto: ClientIdentificationCrypto
    private lateinit var mockCursor: Cursor

    @Before
    fun setUp() {
        mockCursor = mockk(relaxed = true) {
            every { moveToFirst() } returns true
            every { getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_CLIENT_ID) } returns 0
            every { getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_SALT) } returns 1
            every { getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_IV) } returns 2
            every { getString(0) } returns ENCRYPTED_ClIENT_ID
            every { getString(1) } returns SALT
            every { getString(2) } returns IV
        }
        every {
            mockEmarsysContentResolver.query(
                any<Uri>(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockCursor

        mockClientIdentificationCrypto = mockk()
        every {
            mockClientIdentificationCrypto.decrypt(
                any(),
                any(),
                any()
            )
        } returns "CLIENT_ID"
        every { mockClientIdentificationCrypto.encrypt(any()) } returns ClientIdentification(
            ENCRYPTED_ClIENT_ID,
            SALT,
            IV
        )
        contentResolver = ClientIdContentResolver(
            mockEmarsysContentResolver,
            mockClientIdentificationCrypto,
            SHARED_PACKAGE_NAMES
        )
    }

    @Test
    fun testProvideClientId_shouldGetClientId_fromContentResolver() {
        contentResolver.resolveClientId()

        verify { mockClientIdentificationCrypto.decrypt(ENCRYPTED_ClIENT_ID, SALT, IV) }
    }

    @Test
    fun testProvideClientId_shouldNotGetClientId_fromContentResolver() {
        val mockCursor: Cursor = mockk {
            every { moveToFirst() } returns false
            every { getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_CLIENT_ID) } returns 0
            every { getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_SALT) } returns 1
            every { getColumnIndexOrThrow(DatabaseContract.CLIENT_IDENTIFICATION_COLUMN_NAME_IV) } returns 2
            every { getString(0) } returns ENCRYPTED_ClIENT_ID
            every { getString(1) } returns SALT
            every { getString(2) } returns IV
        }
        every {
            mockEmarsysContentResolver.query(
                any<Uri>(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockCursor
        val contentResolver = ClientIdContentResolver(
            mockEmarsysContentResolver,
            mockClientIdentificationCrypto,
            SHARED_PACKAGE_NAMES
        )
        contentResolver.resolveClientId()

        verify(exactly = 0) { mockClientIdentificationCrypto.decrypt(any(), any(), any()) }
        verify(exactly = 0) { mockClientIdentificationCrypto.encrypt(any()) }
    }

    @Test
    fun testProvideClientId_shouldReturnFalse_whenSharedPackageNamesIsMissing() {
        val contentResolver =
            ClientIdContentResolver(
                mockEmarsysContentResolver,
                mockClientIdentificationCrypto,
                null
            )

        val result = contentResolver.resolveClientId()

        verify(exactly = 0) { mockClientIdentificationCrypto.decrypt(any(), any(), any()) }
        verify(exactly = 0) { mockClientIdentificationCrypto.encrypt(any()) }
        result shouldBe null
    }
}
