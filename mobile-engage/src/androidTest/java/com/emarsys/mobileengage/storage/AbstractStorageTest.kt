package com.emarsys.mobileengage.storage

import android.content.SharedPreferences
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class AbstractStorageTest {
    private companion object {
        const val VALUE = "value"
    }

    lateinit var sharedPreferences: SharedPreferences
    lateinit var storage: AbstractStorage<String, SharedPreferences>

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        sharedPreferences = mock(SharedPreferences::class.java)
        storage = (mock(AbstractStorage::class.java, Mockito.CALLS_REAL_METHODS) as AbstractStorage<String, SharedPreferences>).apply {
            ReflectionTestUtils.setInstanceField(this, "store", sharedPreferences)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_storeMustNotBeNull() {
        object : AbstractStorage<String, SharedPreferences>(null) {
            override fun persistValue(store: SharedPreferences?, value: String?) = TODO()

            override fun readPersistedValue(store: SharedPreferences?) = TODO()

            override fun removePersistedValue(store: SharedPreferences?) = TODO()
        }
    }

    @Test
    fun testGet_shouldReturnValueFromMemory() {
        storage.set(VALUE)

        val result = storage.get()

        result shouldBe VALUE
        verify(storage, times(0)).readPersistedValue(sharedPreferences)
    }

    @Test
    fun testSet_shouldPersistValue() {
        storage.set(VALUE)

        verify(storage).persistValue(sharedPreferences, VALUE)
    }

    @Test
    fun testGet_shouldReturnPersistedValueWhenImMemoryNotExists() {
        val persistedValue = "persisted"
        whenever(storage.readPersistedValue(sharedPreferences)).thenReturn(persistedValue)

        storage.remove()

        val result = storage.get()

        result shouldBe persistedValue
    }

    @Test
    fun testGet_shouldCachePersistedValue_inMemory_whenNull() {
        val expected = "persistedAndStoredInMemory"
        whenever(storage.readPersistedValue(sharedPreferences)).thenReturn(expected, null)

        storage.remove()

        storage.get()

        val result = storage.get()
        result shouldBe expected
    }

    @Test
    fun testRemove_shouldRemoveInMemoryValue() {
        storage.set(VALUE)

        storage.remove()

        val inMemoryValue = ReflectionTestUtils.getInstanceField<String>(storage, "value")

        inMemoryValue shouldBe null
    }

    @Test
    fun testRemove_shouldRemovePersistedValue() {
        storage.remove()

        verify(storage).removePersistedValue(sharedPreferences)
    }
}



