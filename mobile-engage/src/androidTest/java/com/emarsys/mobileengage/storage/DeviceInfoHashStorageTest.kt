package com.emarsys.mobileengage.storage

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.testUtil.SharedPrefsUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test

class DeviceInfoHashStorageTest {
    private lateinit var context: Context
    private lateinit var storage: DeviceInfoHashStorage

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        storage = createDeviceInfoHashStorage()
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSet_hash_shouldNotBeNull() {
        storage.set(null)
    }

    @Test
    fun testSet() {
        storage.set(42)
        storage.get() shouldBe 42
    }

    @Test
    fun testClear() {
        storage.set(42)
        storage.remove()

        storage.get() shouldBe null
    }

    @Test
    fun testSetDeviceInfoHashCode_shouldPreserveValues() {
        storage.set(42)
        storage = createDeviceInfoHashStorage()

        storage.get() shouldBe 42
    }

    private fun createDeviceInfoHashStorage(): DeviceInfoHashStorage {
        return DeviceInfoHashStorage(context.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE))
    }
}