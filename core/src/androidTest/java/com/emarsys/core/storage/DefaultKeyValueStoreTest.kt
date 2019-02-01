package com.emarsys.core.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class DefaultKeyValueStoreTest {
    companion object {
        private const val KEY = "key"
        private const val DELTA = 0.001
        private const val KEY1 = "key1"
        private const val KEY2 = "key2"
        private const val KEY3 = "key3"
        private const val KEY4 = "key4"
        private const val KEY5 = "key5"
        private const val KEY6 = "key6"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var store: DefaultKeyValueStore
    private lateinit var prefs: SharedPreferences

    @Before
    @SuppressLint("ApplySharedPref")
    fun init() {
        prefs = InstrumentationRegistry.getTargetContext()
                .applicationContext
                .getSharedPreferences("DefaultKeysStoreTest", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = DefaultKeyValueStore(prefs)
    }

    @After
    fun tearDown() {
        prefs.edit().clear().commit()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_shouldNotAcceptNullContext() {
        DefaultKeyValueStore(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutString_shouldNotAcceptNullKey() {
        store.putString(null, "value")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutString_shouldNotAcceptNullValue() {
        store.putString("key", null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutInt_shouldNotAcceptNullKey() {
        store.putInt(null, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutLong_shouldNotAcceptNullKey() {
        store.putLong(null, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutFloat_shouldNotAcceptNullKey() {
        store.putFloat(null, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutDouble_shouldNotAcceptNullKey() {
        store.putDouble(null, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPutBoolean_shouldNotAcceptNullKey() {
        store.putBoolean(null, false)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetString_shouldNotAcceptNullKey() {
        store.getString(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetInt_shouldNotAcceptNullKey() {
        store.getInt(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetLong_shouldNotAcceptNullKey() {
        store.getLong(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetFloat_shouldNotAcceptNullKey() {
        store.getFloat(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetDouble_shouldNotAcceptNullKey() {
        store.getDouble(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetBoolean_shouldNotAcceptNullKey() {
        store.getBoolean(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRemove_shouldNotAcceptNullKey() {
        store.remove(null)
    }

    @Test
    fun test_put_get_string_shouldStoreValue() {
        store.putString(KEY, "value")
        assertEquals("value", store.getString(KEY))
    }

    @Test
    fun test_put_get_int_shouldStoreValue() {
        store.putInt(KEY, 342)
        assertEquals(342, store.getInt(KEY))
    }

    @Test
    fun test_put_get_long_shouldStoreValue() {
        store.putLong(KEY, 33L)
        assertEquals(33L, store.getLong(KEY))
    }

    @Test
    fun test_put_get_float_shouldStoreValue() {
        store.putFloat(KEY, 2.4f)
        assertEquals(2.4f, store.getFloat(KEY), DELTA.toFloat())
    }

    @Test
    fun test_put_get_double_shouldStoreValue() {
        store.putDouble(KEY, 0.2)
        assertEquals(0.2, store.getDouble(KEY), DELTA)
    }

    @Test
    fun test_put_get_double_shouldStoreValue_extremeValues() {
        store.putDouble(KEY1, java.lang.Double.MAX_VALUE)
        store.putDouble(KEY2, java.lang.Double.MIN_VALUE)
        store.putDouble(KEY3, java.lang.Double.NaN)
        store.putDouble(KEY4, java.lang.Double.POSITIVE_INFINITY)
        store.putDouble(KEY5, java.lang.Double.NEGATIVE_INFINITY)

        assertEquals(java.lang.Double.MAX_VALUE, store.getDouble(KEY1), DELTA)
        assertEquals(java.lang.Double.MIN_VALUE, store.getDouble(KEY2), DELTA)
        assertEquals(java.lang.Double.NaN, store.getDouble(KEY3), DELTA)
        assertEquals(java.lang.Double.POSITIVE_INFINITY, store.getDouble(KEY4), DELTA)
        assertEquals(java.lang.Double.NEGATIVE_INFINITY, store.getDouble(KEY5), DELTA)
    }

    @Test
    fun test_put_get_boolean_shouldStoreValue() {
        store.putBoolean(KEY, true)
        assertEquals(true, store.getBoolean(KEY))
    }

    @Test
    fun testPut_shouldOverridePreviousValues_withTheSameKey() {
        store.putString(KEY, "value")
        assertEquals("value", store.getString(KEY))

        store.putInt(KEY, 23)
        assertEquals(23, store.getInt(KEY))

        store.putLong(KEY, 88111)
        assertEquals(88111, store.getLong(KEY))

        store.putFloat(KEY, 765.23f)
        assertEquals(765.23f, store.getFloat(KEY), DELTA.toFloat())

        store.putDouble(KEY, 0.03013)
        assertEquals(0.03013, store.getDouble(KEY), DELTA)

        store.putBoolean(KEY, true)
        assertEquals(true, store.getBoolean(KEY))
    }

    @Test
    fun testRemove() {
        store.putString(KEY1, "value")
        store.remove(KEY1)
        assertNull(store.getString(KEY1))

        store.putInt(KEY2, 567)
        store.remove(KEY2)
        assertEquals(0, store.getInt(KEY2))

        store.putLong(KEY3, 888)
        store.remove(KEY3)
        assertEquals(0, store.getLong(KEY3))

        store.putFloat(KEY4, 44.2f)
        store.remove(KEY4)
        assertEquals(0.0f, store.getFloat(KEY4), DELTA.toFloat())

        store.putDouble(KEY5, 120120.0301)
        store.remove(KEY5)
        assertEquals(0.0, store.getDouble(KEY5), DELTA)

        store.putBoolean(KEY6, true)
        store.remove(KEY6)
        assertEquals(false, store.getBoolean(KEY6))
    }

    @Test
    fun testRemove_shouldDoNothing_withNonExistentKeyValue() {
        store.putInt(KEY1, 70)
        store.putString(KEY2, "value")

        store.remove(KEY)

        assertEquals(2, store.size)
        assertEquals(70, store.getInt(KEY1))
        assertEquals("value", store.getString(KEY2))
    }

    @Test
    fun testSize_shouldBe_0_afterClear() {
        store.putBoolean(KEY, true)
        store.clear()

        assertEquals(0, store.size)
    }

    @Test
    fun testSize_shouldBeInfluencedBy_put_remove() {
        store.putString(KEY1, "value")
        store.putLong(KEY2, 18)
        store.putBoolean(KEY3, true)

        assertEquals(3, store.size)

        store.remove(KEY2)

        assertEquals(2, store.size)
    }

    @Test
    fun testIsEmpty_shouldBeTrue_afterClear() {
        store.putBoolean(KEY, true)
        store.clear()

        assertTrue(store.isEmpty)
    }

    @Test
    fun testIsEmpty_shouldBeFalse_whenContainsItems() {
        store.putInt("key2", 18)
        store.putInt("key3", 10)

        assertFalse(store.isEmpty)
    }
}