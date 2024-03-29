import android.database.Cursor
import android.net.Uri
import com.emarsys.core.contentresolver.EmarsysContentResolver
import com.emarsys.core.contentresolver.hardwareid.HardwareIdContentResolver
import com.emarsys.core.crypto.HardwareIdentificationCrypto
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.device.HardwareIdentification
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class HardwareIdContentResolverTest : AnnotationSpec() {

    companion object {
        private val SHARED_PACKAGE_NAMES =
            listOf("emarsys.test", "com.emarsys.test", "com.android.test")
        private const val ENCRYPTED_HARDWARE_ID = "encrypted_shared_hardware_id"
        private const val SALT = "testSalt"
        private const val IV = "testIv"

    }

    private var mockEmarsysContentResolver: EmarsysContentResolver = mockk()
    private lateinit var contentResolver: HardwareIdContentResolver
    private lateinit var mockHardwareIdentificationCrypto: HardwareIdentificationCrypto
    private lateinit var mockCursor: Cursor

    @Before
    fun setUp() {
        mockCursor = mockk(relaxed = true) {
            every { moveToFirst() } returns true
            every { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID) } returns 0
            every { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT) } returns 1
            every { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV) } returns 2
            every { getString(0) } returns ENCRYPTED_HARDWARE_ID
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

        mockHardwareIdentificationCrypto = mockk()
        every {
            mockHardwareIdentificationCrypto.decrypt(
                any(),
                any(),
                any()
            )
        } returns "HARDWARE_ID"
        every { mockHardwareIdentificationCrypto.encrypt(any()) } returns HardwareIdentification(
            ENCRYPTED_HARDWARE_ID,
            SALT,
            IV
        )
        contentResolver = HardwareIdContentResolver(
            mockEmarsysContentResolver,
            mockHardwareIdentificationCrypto,
            SHARED_PACKAGE_NAMES
        )
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContentResolver() {
        contentResolver.resolveHardwareId()

        verify { mockHardwareIdentificationCrypto.decrypt(ENCRYPTED_HARDWARE_ID, SALT, IV) }
    }

    @Test
    fun testProvideHardwareId_shouldNotGetHardwareId_fromContentResolver() {
        val mockCursor: Cursor = mockk {
            every { moveToFirst() } returns false
            every { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID) } returns 0
            every { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT) } returns 1
            every { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV) } returns 2
            every { getString(0) } returns ENCRYPTED_HARDWARE_ID
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
        val contentResolver = HardwareIdContentResolver(
            mockEmarsysContentResolver,
            mockHardwareIdentificationCrypto,
            SHARED_PACKAGE_NAMES
        )
        contentResolver.resolveHardwareId()

        verify(exactly = 0) { mockHardwareIdentificationCrypto.decrypt(any(), any(), any()) }
        verify(exactly = 0) { mockHardwareIdentificationCrypto.encrypt(any()) }
    }

    @Test
    fun testProvideHardwareId_shouldReturnFalse_whenSharedPackageNamesIsMissing() {
        val contentResolver =
            HardwareIdContentResolver(
                mockEmarsysContentResolver,
                mockHardwareIdentificationCrypto,
                null
            )

        val result = contentResolver.resolveHardwareId()

        verify(exactly = 0) { mockHardwareIdentificationCrypto.decrypt(any(), any(), any()) }
        verify(exactly = 0) { mockHardwareIdentificationCrypto.encrypt(any()) }
        result shouldBe null
    }
}
