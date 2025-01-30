package com.emarsys.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class SharedClientIdentificationContentProviderTest  {

    private companion object {
        const val ENCRYPTED_CLIENT_ID = "encrypted_hardware_id"
        const val SALT = "salt"
        const val IV = "iv"
    }

    private lateinit var mockCoreDbHelper: CoreDbHelper
    private lateinit var mockDatabase: CoreSQLiteDatabase
    private lateinit var mockCursor: Cursor
    private lateinit var provider: SharedHardwareIdentificationContentProvider

    @Before
    fun setUp() {
        mockCursor = mockk {
            every { getString(0) } returns ENCRYPTED_CLIENT_ID
            every { getString(1) } returns SALT
            every { getString(2) } returns IV
        }
        mockDatabase = mockk {
            every {
                query(
                    false,
                    "hardware_identification",
                    arrayOf("encrypted_hardware_id", "salt", "iv"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            } returns mockCursor
        }
        mockCoreDbHelper = mockk {
            every { readableCoreDatabase } returns mockDatabase
        }
        provider = SharedHardwareIdentificationContentProvider().apply {
            coreDbHelper = mockCoreDbHelper
        }
    }

    @Test
    fun testOnCreate_shouldCreateCoreDbHelper() {
        val result = provider.onCreate()
        result shouldBe true
    }

    @Test
    fun testQuery_shouldReturnCursorWithEncryptedClientId_salt_iv() {
        val mockContext: Context = mockk {
            every { packageName } returns "com.emarsys.test"
        }

        ReflectionTestUtils.setInstanceField(provider, "mContext", mockContext)

        val cursor = provider.query(
            Uri.parse("content://com.emarsys.test/hardware_identification"),
            null, null, null, null
        )

        cursor shouldBe mockCursor
    }

    @Test
    fun testQuery_shouldReturnNull_whenInvalidRequest() {
        val mockContext: Context = mockk {
            every { packageName } returns "com.emarsys.test"
        }

        ReflectionTestUtils.setInstanceField(provider, "mContext", mockContext)

        val cursor = provider.query(
            Uri.parse("content://com.emarsys.test/hardware/other"),
            null,
            null,
            null,
            null
        )
        cursor shouldBe null
    }
}