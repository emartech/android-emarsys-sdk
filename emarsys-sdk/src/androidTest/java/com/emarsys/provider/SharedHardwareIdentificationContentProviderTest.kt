package com.emarsys.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.test.ProviderTestCase2
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class SharedHardwareIdentificationContentProviderTest : ProviderTestCase2<SharedHardwareIdentificationContentProvider>(SharedHardwareIdentificationContentProvider::class.java, "com.emarsys.test") {

    private companion object {
        const val ENCRYPTED_HARDWARE_ID = "encrypted_hardware_id"
        const val SALT = "salt"
        const val IV = "iv"
    }

    lateinit var mockCoreDbHelper: CoreDbHelper
    lateinit var mockDatabase: CoreSQLiteDatabase
    lateinit var mockCursor: Cursor

    @Before
    override fun setUp() {
        super.setUp()
        mockCursor = mock {
            on { getString(0) } doReturn ENCRYPTED_HARDWARE_ID
            on { getString(1) } doReturn SALT
            on { getString(2) } doReturn IV
        }
        mockDatabase = mock {
            on {
                query(false, "hardware_identification", arrayOf("encrypted_hardware_id", "salt", "iv"),
                        null, null, null, null, null, null)
            } doReturn mockCursor
        }
        mockCoreDbHelper = mock {
            on { readableCoreDatabase } doReturn mockDatabase
        }
    }

    @Test
    fun testOnCreate_shouldCreateCoreDbHelper() {
        ReflectionTestUtils.setInstanceField(provider, "coreDbHelper", null)
        var result = ReflectionTestUtils.getInstanceField<CoreDbHelper>(provider, "coreDbHelper")
        result shouldBe null

        provider.onCreate()

        result = ReflectionTestUtils.getInstanceField<CoreDbHelper>(provider, "coreDbHelper")
        result shouldNotBe null
    }

    @Test
    fun testQuery_shouldReturnCursorWithEncryptedHardwareId_salt_iv() {
        val mockContext: Context = mock {
            on { packageName } doReturn "com.emarsys.test"
        }
        ReflectionTestUtils.setInstanceField(provider, "coreDbHelper", mockCoreDbHelper)
        ReflectionTestUtils.setInstanceField(provider, "mContext", mockContext)

        val cursor = provider.query(Uri.parse("content://com.emarsys.test/hardware_identification"),
                null, null, null, null)

        cursor shouldNotBe null
        cursor shouldBe mockCursor
    }

    @Test
    fun testQuery_shouldReturnNull_whenInvalidRequest() {
        val mockContext: Context = mock {
            on { packageName } doReturn "com.emarsys.test"
        }
        ReflectionTestUtils.setInstanceField(provider, "coreDbHelper", mockCoreDbHelper)
        ReflectionTestUtils.setInstanceField(provider, "mContext", mockContext)

        val cursor = provider.query(Uri.parse("content://com.emarsys.test/hardware/other"), null, null, null, null)
        cursor shouldBe null
    }
}