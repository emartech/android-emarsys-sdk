import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.crypto.SharedPreferenceCrypto
import com.emarsys.core.storage.EmarsysEncryptedSharedPreferencesV3
import com.emarsys.core.storage.EncryptedSharedPreferencesToSharedPreferencesMigration
import com.emarsys.core.storage.SharedPreferencesV3Provider
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SharedPreferencesV3ProviderTest  {

    private lateinit var mockContext: Context
    private lateinit var mockOldSharedPreferences: SharedPreferences
    private lateinit var mockCrypto: SharedPreferenceCrypto
    private lateinit var mockMigration: EncryptedSharedPreferencesToSharedPreferencesMigration
    private lateinit var mockEmarsysEncryptedSharedPreferencesV3: EmarsysEncryptedSharedPreferencesV3

    @Before
    fun setup() {
        mockContext = mockk()
        mockOldSharedPreferences = mockk()
        mockCrypto = mockk()
        mockMigration = mockk()
        mockEmarsysEncryptedSharedPreferencesV3 = mockk(relaxed = true)
        val mockRealSharedPrefs: SharedPreferences = mockk(relaxed = true)
        every {
            mockContext.getSharedPreferences(
                any(),
                any()
            )
        } returns mockRealSharedPrefs
        every { mockCrypto.getOrCreateSecretKey() } returns mockk()
        every { mockMigration.migrate(any(), any()) } just Runs

    }

    @Test
    fun testInitialization() {
        val provider = SharedPreferencesV3Provider(
            mockContext,
            "test_file",
            mockOldSharedPreferences,
            mockCrypto,
            mockMigration
        )
        verify {
            mockMigration.migrate(
                mockOldSharedPreferences,
                any()
            )
        }
    }

    @Test
    fun testProvide() {
        val provider = SharedPreferencesV3Provider(
            mockContext,
            "test_file",
            mockOldSharedPreferences,
            mockCrypto,
            mockMigration
        )
        ReflectionTestUtils.setInstanceField(
            provider,
            "sharedPreferences",
            mockEmarsysEncryptedSharedPreferencesV3
        )
        val result = provider.provide()

        result shouldBe mockEmarsysEncryptedSharedPreferencesV3
    }

    @Test
    fun testMigrationIsCalledOnlyOnce() {
        val provider = SharedPreferencesV3Provider(
            mockContext,
            "test_file",
            mockOldSharedPreferences,
            mockCrypto,
            mockMigration
        )

        provider.provide()
        provider.provide()

        verify(exactly = 1) { mockMigration.migrate(any(), any()) }
    }
}