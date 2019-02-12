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

class ContactTokenStorageTest {
    private lateinit var context: Context
    private lateinit var storage: ContactTokenStorage

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        storage = createContactTokenStorage()
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSet_contactToken_shouldNotBeNull() {
        storage.set(null)
    }

    @Test
    fun testSet() {
        storage.set("contact-token")
        storage.get() shouldBe "contact-token"
    }

    @Test
    fun testClear() {
        storage.set("contact-token")
        storage.remove()

        storage.get() shouldBe null
    }

    @Test
    fun testSetContactTokenStorage_shouldPreserveValues() {
        storage.set("contact-token")
        storage = createContactTokenStorage()

        storage.get() shouldBe "contact-token"
    }

    private fun createContactTokenStorage(): ContactTokenStorage {
        return ContactTokenStorage(context.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE))
    }
}