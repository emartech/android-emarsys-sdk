package com.emarsys.core.storage

import android.content.SharedPreferences
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class BooleanStorageTest {

    private companion object {
        const val VALUE = true
        const val KEY = "key"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var storage: BooleanStorage
    private lateinit var storageKey: StorageKey

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        storageKey = mock(StorageKey::class.java).apply {
            whenever(key).thenReturn(KEY)
        }

        editor = mock(SharedPreferences.Editor::class.java).apply {
            whenever(putBoolean(anyString(), anyBoolean())).thenReturn(this)
            whenever(remove(anyString())).thenReturn(this)
        }

        sharedPreferences = mock(SharedPreferences::class.java).apply {
            whenever(edit()).thenReturn(editor)
        }

        storage = BooleanStorage(storageKey, sharedPreferences)
    }

    @Test(expected = NullPointerException::class)
    fun testConstructor_valueReturnedByKey_mustNotBeNull() {
        BooleanStorage(mock(StorageKey::class.java), sharedPreferences)
    }

    @Test
    fun testPersistValue() {
        storage.persistValue(sharedPreferences, VALUE)

        verify(sharedPreferences).edit()
        verify(editor).putBoolean(KEY, VALUE)
        verify(editor).apply()

        verifyNoMoreInteractions(editor)
        verifyNoMoreInteractions(sharedPreferences)
    }

    @Test
    fun testReadPersistedValue() {
        storage.readPersistedValue(sharedPreferences)

        verify(sharedPreferences).getBoolean(KEY, false)
        verifyNoMoreInteractions(sharedPreferences)
    }

    @Test
    fun testRemovePersistedValue() {
        storage.removePersistedValue(sharedPreferences)

        verify(sharedPreferences).edit()
        verify(editor).remove(KEY)
        verify(editor).apply()

        verifyNoMoreInteractions(editor)
        verifyNoMoreInteractions(sharedPreferences)
    }
}
