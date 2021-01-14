package com.emarsys.provider

import android.database.Cursor
import android.net.Uri
import android.test.ProviderTestCase2
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.testUtil.ReflectionTestUtils
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Test


class SharedHardwareIdProviderTest : ProviderTestCase2<SharedHardwareIdProvider>(SharedHardwareIdProvider::class.java, "com.emarsys") {

    private companion object {
        const val HARDWARE_ID = "hardwareId"
    }

    lateinit var mockCoreDbHelper: CoreDbHelper
    lateinit var mockDatabase: CoreSQLiteDatabase
    lateinit var mockCursor: Cursor

    @Before
    override fun setUp() {
        super.setUp()
        mockCursor = mock {
            on { getString(0) } doReturn HARDWARE_ID
        }
        mockDatabase = mock {
            on { query(false, "hardware", arrayOf("hardware_id"), null, null, null, null, null, null) } doReturn mockCursor
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
    fun testQuery_shouldReturnCursorWithHardwareId() {
        ReflectionTestUtils.setInstanceField(provider, "coreDbHelper", mockCoreDbHelper)

        val cursor = provider.query(Uri.parse("content://com.emarsys/hardware/hardware_id"), null, null, null, null)

        cursor shouldNotBe null
        val result = cursor?.getString(0)
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testQuery_shouldReturnNull_whenInvalidRequest() {
        ReflectionTestUtils.setInstanceField(provider, "coreDbHelper", mockCoreDbHelper)

        val cursor = provider.query(Uri.parse("content://com.emarsys/hardware/other"), null, null, null, null)
        cursor shouldBe null
    }
}