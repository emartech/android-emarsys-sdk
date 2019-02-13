package com.emarsys.mobileengage.storage

import android.content.SharedPreferences
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class StringStorageTest {

    private companion object {
        const val VALUE = "value"
        const val KEY = "key"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var storage: StringStorage
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
            whenever(putString(anyString(), anyString())).thenReturn(this)
            whenever(remove(anyString())).thenReturn(this)
        }

        sharedPreferences = mock(SharedPreferences::class.java).apply {
            whenever(edit()).thenReturn(editor)
        }

        storage = StringStorage(storageKey, sharedPreferences)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_key_mustNotBeNull() {
        StringStorage(null, sharedPreferences)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_valueReturnedByKey_mustNotBeNull() {
        StringStorage(mock(StorageKey::class.java), sharedPreferences)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_sharedPreference_mustNotBeNull() {
        StringStorage(storageKey, null)
    }

    @Test
    fun testPersistValue() {
        storage.persistValue(sharedPreferences, VALUE)

        verify(sharedPreferences).edit()
        verify(editor).putString(KEY, VALUE)
        verify(editor).apply()

        verifyNoMoreInteractions(editor)
        verifyNoMoreInteractions(sharedPreferences)
    }

    @Test
    fun testReadPersistedValue() {
        storage.readPersistedValue(sharedPreferences)

        verify(sharedPreferences).getString(KEY, null)
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
