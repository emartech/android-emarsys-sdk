package com.emarsys.core.storage

import android.content.SharedPreferences
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class AbstractStorageTest : AnnotationSpec() {
    private companion object {
        const val VALUE = "value"
    }

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockStorage: AbstractStorage<String, SharedPreferences>


    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockSharedPreferences = mock()
        mockStorage =
            (mock(defaultAnswer = CALLS_REAL_METHODS) as AbstractStorage<String, SharedPreferences>)
        ReflectionTestUtils.setInstanceField(mockStorage, "store", mockSharedPreferences)
    }

    @Test
    fun testConstructor_storeMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            object : AbstractStorage<String, SharedPreferences>(null) {
                override fun persistValue(store: SharedPreferences?, value: String?) = TODO()

                override fun readPersistedValue(store: SharedPreferences?) = TODO()

                override fun removePersistedValue(store: SharedPreferences?) = TODO()
            }
        }
    }

    @Test
    fun testGet_shouldReturnValueFromMemory() {
        mockStorage.set(VALUE)

        val result = mockStorage.get()

        result shouldBe VALUE
        verify(mockStorage, times(0)).readPersistedValue(mockSharedPreferences)
    }

    @Test
    fun testSet_shouldPersistValue() {
        mockStorage.set(VALUE)

        verify(mockStorage).persistValue(mockSharedPreferences, VALUE)
    }

    @Test
    fun testGet_shouldReturnPersistedValueWhenImMemoryNotExists() {
        val persistedValue = "persisted"
        whenever(mockStorage.readPersistedValue(mockSharedPreferences)).thenReturn(persistedValue)

        mockStorage.remove()

        val result = mockStorage.get()

        result shouldBe persistedValue
    }

    @Test
    fun testGet_shouldCachePersistedValue_inMemory_whenNull() {
        val expected = "persistedAndStoredInMemory"
        whenever(mockStorage.readPersistedValue(mockSharedPreferences)).thenReturn(expected, null)

        mockStorage.remove()

        mockStorage.get()

        val result = mockStorage.get()
        result shouldBe expected
    }

    @Test
    fun testRemove_shouldRemoveInMemoryValue() {
        mockStorage.set(VALUE)

        mockStorage.remove()

        val inMemoryValue = ReflectionTestUtils.getInstanceField<String>(mockStorage, "value")

        inMemoryValue shouldBe null
    }

    @Test
    fun testRemove_shouldRemovePersistedValue() {
        mockStorage.remove()

        verify(mockStorage).removePersistedValue(mockSharedPreferences)
    }
}



