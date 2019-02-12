package com.emarsys.mobileengage.storage

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class RefreshTokenStorageTest {
    private lateinit var context: Context
    private lateinit var storage: RefreshTokenStorage

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        storage = createRefreshTokenStorage()
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSet_contactToken_shouldNotBeNull() {
        storage.set(null)
    }

    @Test
    fun testSet() {
        storage.set("refresh-token")
        storage.get() shouldBe "refresh-token"
    }

    @Test
    fun testClear() {
        storage.set("refresh-token")
        storage.remove()

        storage.get() shouldBe null
    }

    @Test
    fun testSetRefreshTokenStorage_shouldPreserveValues() {
        storage.set("refresh-token")
        storage = createRefreshTokenStorage()

        storage.get() shouldBe "refresh-token"
    }

    private fun createRefreshTokenStorage(): RefreshTokenStorage {
        return RefreshTokenStorage(context.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE))
    }
}