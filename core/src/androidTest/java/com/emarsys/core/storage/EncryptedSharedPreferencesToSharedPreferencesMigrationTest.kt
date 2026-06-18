package com.emarsys.core.storage

import android.content.SharedPreferences
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.security.GeneralSecurityException

class EncryptedSharedPreferencesToSharedPreferencesMigrationTest {

    private val mockOldSharedPreferences = mockk<SharedPreferences>()
    private val mockNewSharedPreferences = mockk<SharedPreferences>()
    private val mockOldEditor = mockk<SharedPreferences.Editor>()
    private val mockNewEditor = mockk<SharedPreferences.Editor>()

    @Test
    fun shouldMigrateData_from_oldSharedPreferences_to_newSharedPreferences() {
        every { mockOldSharedPreferences.all } returns mapOf(
            "string_key" to "value",
            "int_key" to 42,
            "boolean_key" to true,
            "float_key" to 3.14f,
            "long_key" to 1234L,
            "set_key" to setOf("item1", "item2")
        )
        every { mockNewSharedPreferences.edit() } returns mockNewEditor
        every { mockNewEditor.putString(any(), any()) } returns mockNewEditor
        every { mockNewEditor.putInt(any(), any()) } returns mockNewEditor
        every { mockNewEditor.putBoolean(any(), any()) } returns mockNewEditor
        every { mockNewEditor.putFloat(any(), any()) } returns mockNewEditor
        every { mockNewEditor.putLong(any(), any()) } returns mockNewEditor
        every { mockNewEditor.putStringSet(any(), any()) } returns mockNewEditor
        every { mockNewEditor.apply() } just Runs
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs

        EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
            mockOldSharedPreferences,
            mockNewSharedPreferences
        )

        verify { mockNewEditor.putString("string_key", "value") }
        verify { mockNewEditor.putInt("int_key", 42) }
        verify { mockNewEditor.putBoolean("boolean_key", true) }
        verify { mockNewEditor.putFloat("float_key", 3.14f) }
        verify { mockNewEditor.putLong("long_key", 1234L) }
        verify { mockNewEditor.putStringSet("set_key", setOf("item1", "item2")) }
        verify { mockNewEditor.apply() }
        verify { mockOldEditor.clear() }
        verify { mockOldEditor.apply() }
    }

    @Test
    fun shouldClearBothOldAndNewPrefs_whenGetAllThrows_preventingBrokenStateOnRestart() {
        every { mockOldSharedPreferences.all } throws SecurityException("Could not decrypt key. decryption failed")
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs
        every { mockNewSharedPreferences.edit() } returns mockNewEditor
        every { mockNewEditor.clear() } returns mockNewEditor
        every { mockNewEditor.apply() } just Runs

        EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
            mockOldSharedPreferences,
            mockNewSharedPreferences
        )

        verify { mockOldEditor.clear() }
        verify { mockOldEditor.apply() }
        verify { mockNewEditor.clear() }
        verify { mockNewEditor.apply() }
    }

    @Test
    fun shouldClearBothOldAndNewPrefs_whenGetAllThrowsIllegalArgumentException_badBase64() {
        every { mockOldSharedPreferences.all } throws IllegalArgumentException("bad base-64")
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs
        every { mockNewSharedPreferences.edit() } returns mockNewEditor
        every { mockNewEditor.clear() } returns mockNewEditor
        every { mockNewEditor.apply() } just Runs

        EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
            mockOldSharedPreferences,
            mockNewSharedPreferences
        )

        verify { mockOldEditor.clear() }
        verify { mockOldEditor.apply() }
        verify { mockNewEditor.clear() }
        verify { mockNewEditor.apply() }
    }

    @Test
    fun shouldClearBothPrefs_whenPerKeyWriteThrows() {
        every { mockOldSharedPreferences.all } returns linkedMapOf(
            "key_before" to "value_before",
            "key_bad" to "value_bad",
            "key_after" to "value_after"
        )
        every { mockNewSharedPreferences.edit() } returns mockNewEditor
        every { mockNewEditor.putString("key_before", "value_before") } returns mockNewEditor
        every { mockNewEditor.putString("key_bad", "value_bad") } throws GeneralSecurityException("Encryption error")
        every { mockNewEditor.clear() } returns mockNewEditor
        every { mockNewEditor.apply() } just Runs
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs

        EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
            mockOldSharedPreferences,
            mockNewSharedPreferences
        )

        verify { mockNewEditor.putString("key_before", "value_before") }
        verify(exactly = 0) { mockNewEditor.putString("key_after", "value_after") }
        verify { mockOldEditor.clear() }
        verify { mockOldEditor.apply() }
        verify { mockNewEditor.clear() }
    }

    @Test
    fun shouldNotCrash_whenClearingNewPrefsFailsInOuterCatch() {
        every { mockOldSharedPreferences.all } throws SecurityException("decryption failed")
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs
        every { mockNewSharedPreferences.edit() } returns mockNewEditor
        every { mockNewEditor.clear() } returns mockNewEditor
        every { mockNewEditor.apply() } throws IllegalStateException("apply failed")

        shouldNotThrowAny {
            EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
                mockOldSharedPreferences,
                mockNewSharedPreferences
            )
        }

        verify { mockOldEditor.clear() }
        verify { mockOldEditor.apply() }
        verify { mockNewEditor.clear() }
    }

    @Test
    fun shouldStillClearOldPrefs_whenClearingNewPrefsFailsInOuterCatch() {
        every { mockOldSharedPreferences.all } throws SecurityException("decryption failed")
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs
        every { mockNewSharedPreferences.edit() } throws IllegalStateException("cannot edit")

        EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
            mockOldSharedPreferences,
            mockNewSharedPreferences
        )

        verify { mockOldEditor.clear() }
        verify { mockOldEditor.apply() }
    }

    @Test
    fun shouldNotThrowAnyExceptionsDuringSuccessfulMigration() {
        every { mockOldSharedPreferences.all } returns mapOf("key" to "value")
        every { mockNewSharedPreferences.edit() } returns mockNewEditor
        every { mockNewEditor.putString(any(), any()) } returns mockNewEditor
        every { mockNewEditor.apply() } just Runs
        every { mockOldSharedPreferences.edit() } returns mockOldEditor
        every { mockOldEditor.clear() } returns mockOldEditor
        every { mockOldEditor.apply() } just Runs

        shouldNotThrowAny {
            EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
                mockOldSharedPreferences,
                mockNewSharedPreferences
            )
        }
    }
}
