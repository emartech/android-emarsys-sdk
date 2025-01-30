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

class EncryptedSharedPreferencesToSharedPreferencesMigrationTest  {

    private val mockOldSharedPreferences = mockk<SharedPreferences>()
    private val mockNewSharedPreferences = mockk<SharedPreferences>()
    private val mockEditor = mockk<SharedPreferences.Editor>()

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
        every { mockNewSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putFloat(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.putStringSet(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockOldSharedPreferences.edit() } returns mockEditor
        every { mockEditor.clear() } returns mockEditor

        val encryptedSharedPreferencesToSharedPreferencesMigration =
            EncryptedSharedPreferencesToSharedPreferencesMigration()
        encryptedSharedPreferencesToSharedPreferencesMigration.migrate(
            mockOldSharedPreferences,
            mockNewSharedPreferences
        )

        verify { mockNewSharedPreferences.edit() }
        verify { mockEditor.putString("string_key", "value") }
        verify { mockEditor.putInt("int_key", 42) }
        verify { mockEditor.putBoolean("boolean_key", true) }
        verify { mockEditor.putFloat("float_key", 3.14f) }
        verify { mockEditor.putLong("long_key", 1234L) }
        verify { mockEditor.putStringSet("set_key", setOf("item1", "item2")) }
        verify { mockEditor.apply() }
        verify { mockOldSharedPreferences.edit() }
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }

    @Test
    fun shouldHandleGeneralSecurityExceptionDuringMigration() {
        every { mockOldSharedPreferences.all } returns mapOf("key" to "value")
        every { mockNewSharedPreferences.edit() } returns mockEditor
        every {
            mockEditor.putString(
                any(),
                any()
            )
        } throws GeneralSecurityException("Encryption error")

        val migration = EncryptedSharedPreferencesToSharedPreferencesMigration()

        migration.migrate(mockOldSharedPreferences, mockNewSharedPreferences)

        verify(exactly = 0) { mockEditor.apply() }
        verify(exactly = 0) { mockOldSharedPreferences.edit().clear() }
    }

    @Test
    fun shouldNotThrowAnyExceptionsDuringSuccessfulMigration() {
        every { mockOldSharedPreferences.all } returns mapOf("key" to "value")
        every { mockNewSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockOldSharedPreferences.edit() } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } just Runs

        val encryptedSharedPreferencesToSharedPreferencesMigration =
            EncryptedSharedPreferencesToSharedPreferencesMigration()
        shouldNotThrowAny {
            encryptedSharedPreferencesToSharedPreferencesMigration.migrate(
                mockOldSharedPreferences,
                mockNewSharedPreferences
            )
        }
    }
}