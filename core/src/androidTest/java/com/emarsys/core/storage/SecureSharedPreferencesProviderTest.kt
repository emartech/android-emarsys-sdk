package com.emarsys.core.storage

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


class SecureSharedPreferencesProviderTest : AnnotationSpec() {

    private lateinit var sharedPreferencesProvider: SecureSharedPreferencesProvider


    private lateinit var context: Context
    private lateinit var oldSharedPrefs: SharedPreferences

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext().applicationContext
        oldSharedPrefs = context.getSharedPreferences("test_shared_prefs", MODE_PRIVATE)

        with(oldSharedPrefs.edit()) {
            putString("string", "testString")
            putInt("int", 1)
            putFloat("float", 0.1f)
            putBoolean("boolean", true)
            commit()
        }

        sharedPreferencesProvider = SecureSharedPreferencesProvider(
                context,
                "emarsys_sdk_secure_shared_pref_test",
                oldSharedPrefs
        )
    }

    @Test
    fun testProvide() {
        val prefs = sharedPreferencesProvider.provide()

        prefs shouldNotBe null
    }

    @Test
    fun testMigration() {

        val prefs = sharedPreferencesProvider.provide()

        prefs.getBoolean("boolean", false) shouldBe true
        prefs.getString("string", "") shouldBe "testString"
        prefs.getInt("int", 0) shouldBe 1
        prefs.getFloat("float", 0.0f) shouldBe 0.1f

        oldSharedPrefs.getBoolean("boolean", false) shouldBe false
        oldSharedPrefs.getString("string", "") shouldBe ""
        oldSharedPrefs.getInt("int", 0) shouldBe 0
        oldSharedPrefs.getFloat("float", 0.0f) shouldBe 0.0f
    }

}