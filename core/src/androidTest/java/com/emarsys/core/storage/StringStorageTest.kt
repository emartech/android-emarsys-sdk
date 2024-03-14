package com.emarsys.core.storage

import android.content.SharedPreferences
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

class StringStorageTest : AnnotationSpec() {

    private companion object {
        const val VALUE = "value"
        const val KEY = "key"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var storage: StringStorage
    private lateinit var storageKey: StorageKey


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

    @Test
    fun testConstructor_key_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            StringStorage(null, sharedPreferences)
        }
    }

    @Test
    fun testConstructor_valueReturnedByKey_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            StringStorage(mock(StorageKey::class.java), sharedPreferences)
        }
    }

    @Test
    fun testConstructor_sharedPreference_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            StringStorage(storageKey, null)
        }
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
