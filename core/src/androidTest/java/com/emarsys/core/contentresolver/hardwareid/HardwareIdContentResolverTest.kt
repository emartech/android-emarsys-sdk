package com.emarsys.core.contentresolver.hardwareid

import android.database.Cursor
import android.net.Uri
import android.test.ProviderTestCase2
import android.test.mock.MockContentProvider
import com.emarsys.core.crypto.HardwareIdentificationCrypto
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.provider.hardwareid.HardwareIdProviderTest
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions

class HardwareIdContentResolverTest : ProviderTestCase2<FakeContentProvider>(FakeContentProvider::class.java, "com.emarsys.test") {

    companion object {
        private val SHARED_PACKAGE_NAMES = listOf("emarsys.test", "com.emarsys.test", "com.android.test")
        private const val ENCRYPTED_HARDWARE_ID = "encrypted_shared_hardware_id"
        private const val SALT = "testSalt"
        private const val IV = "testIv"

    }

    private lateinit var contentResolver: HardwareIdContentResolver
    private lateinit var mockHardwareIdentificationCrypto: HardwareIdentificationCrypto

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    override fun setUp() {
        super.setUp()
        mockHardwareIdentificationCrypto = mock()
        contentResolver = HardwareIdContentResolver(mockContext, mockHardwareIdentificationCrypto, SHARED_PACKAGE_NAMES)
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContentResolver() {
        FakeContentProvider.numberOfInvocation = 0

        contentResolver.resolveHardwareId()

        verify(mockHardwareIdentificationCrypto).decrypt(ENCRYPTED_HARDWARE_ID, SALT, IV)
        FakeContentProvider.numberOfInvocation shouldBe 1
    }

    @Test
    fun testProvideHardwareId_shouldReturnFalse_whenSharedPackageNamesIsMissing() {
        FakeContentProvider.numberOfInvocation = 0
        val contentResolver = HardwareIdContentResolver(mockContext, mockHardwareIdentificationCrypto, null)

        val result = contentResolver.resolveHardwareId()

        FakeContentProvider.numberOfInvocation = 0
        verifyZeroInteractions(mockHardwareIdentificationCrypto)
        result shouldBe null
    }
}

open class FakeContentProvider : MockContentProvider() {
    companion object {
        var numberOfInvocation = 0
    }

    private var cursor: Cursor = mock {
        on { moveToFirst() } doReturn true
        on { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID) } doReturn 0
        on { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT) } doReturn 1
        on { getColumnIndexOrThrow(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV) } doReturn 2
        on { getString(0) } doReturn HardwareIdProviderTest.ENCRYPTED_HARDWARE_ID
        on { getString(1) } doReturn HardwareIdProviderTest.SALT
        on { getString(2) } doReturn HardwareIdProviderTest.IV
    }

    override fun query(uri: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        numberOfInvocation++
        return cursor
    }
}