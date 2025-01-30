package com.emarsys.core.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DefaultKeyValueStoreTest  {
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

    @Test
    fun testConstructor_shouldNotAcceptNullContext() {
        shouldThrow<IllegalArgumentException> {
            DefaultKeyValueStore(null)
        }
    }

    @Test
    fun testPutString_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.putString(null, "value")
        }
    }

    @Test
    fun testPutString_shouldNotAcceptNullValue() {
        shouldThrow<IllegalArgumentException> {
            store.putString("key", null)
        }
    }

    @Test
    fun testPutInt_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.putInt(null, 0)
        }
    }

    @Test
    fun testPutLong_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.putLong(null, 0)
        }
    }

    @Test
    fun testPutFloat_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.putFloat(null, 0f)
        }
    }

    @Test
    fun testPutDouble_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.putDouble(null, 0.0)
        }
    }

    @Test
    fun testPutBoolean_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.putBoolean(null, false)
        }
    }

    @Test
    fun testGetString_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.getString(null)
        }
    }

    @Test
    fun testGetInt_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.getInt(null)
        }
    }

    @Test
    fun testGetLong_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.getLong(null)
        }
    }

    @Test
    fun testGetFloat_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.getFloat(null)
        }
    }

    @Test
    fun testGetDouble_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.getDouble(null)
        }
    }

    @Test
    fun testGetBoolean_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.getBoolean(null)
        }
    }

    @Test
    fun testRemove_shouldNotAcceptNullKey() {
        shouldThrow<IllegalArgumentException> {
            store.remove(null)
        }
    }

    @Test
    fun test_put_get_string_shouldStoreValue() {
        store.putString(KEY, "value")
        store.getString(KEY) shouldBe "value"
    }

    @Test
    fun test_put_get_int_shouldStoreValue() {
        store.putInt(KEY, 342)
        store.getInt(KEY) shouldBe 342
    }

    @Test
    fun test_put_get_long_shouldStoreValue() {
        store.putLong(KEY, 33L)
        store.getLong(KEY) shouldBe 33L
    }

    @Test
    fun test_put_get_float_shouldStoreValue() {
        store.putFloat(KEY, 2.4f)
        store
        Assert.assertEquals(2.4f, store.getFloat(KEY), DELTA.toFloat())
    }

    @Test
    fun test_put_get_double_shouldStoreValue() {
        store.putDouble(KEY, 0.2)
        Assert.assertEquals(0.2, store.getDouble(KEY), DELTA)
    }

    @Test
    fun test_put_get_double_shouldStoreValue_extremeValues() {
        store.putDouble(KEY1, java.lang.Double.MAX_VALUE)
        store.putDouble(KEY2, java.lang.Double.MIN_VALUE)
        store.putDouble(KEY3, java.lang.Double.NaN)
        store.putDouble(KEY4, java.lang.Double.POSITIVE_INFINITY)
        store.putDouble(KEY5, java.lang.Double.NEGATIVE_INFINITY)

        Assert.assertEquals(java.lang.Double.MAX_VALUE, store.getDouble(KEY1), DELTA)
        Assert.assertEquals(java.lang.Double.MIN_VALUE, store.getDouble(KEY2), DELTA)
        Assert.assertEquals(java.lang.Double.NaN, store.getDouble(KEY3), DELTA)
        Assert.assertEquals(java.lang.Double.POSITIVE_INFINITY, store.getDouble(KEY4), DELTA)
        Assert.assertEquals(java.lang.Double.NEGATIVE_INFINITY, store.getDouble(KEY5), DELTA)
    }

    @Test
    fun test_put_get_boolean_shouldStoreValue() {
        store.putBoolean(KEY, true)
        store.getBoolean(KEY) shouldBe true
    }

    @Test
    fun testPut_shouldOverridePreviousValues_withTheSameKey() {
        store.putString(KEY, "value")
        store.getString(KEY) shouldBe "value"

        store.putInt(KEY, 23)
        store.getInt(KEY) shouldBe 23

        store.putLong(KEY, 88111)
        store.getLong(KEY) shouldBe 88111

        store.putFloat(KEY, 765.23f)
        Assert.assertEquals(765.23f, store.getFloat(KEY), DELTA.toFloat())

        store.putDouble(KEY, 0.03013)
        Assert.assertEquals(0.03013, store.getDouble(KEY), DELTA)

        store.putBoolean(KEY, true)
        store.getBoolean(KEY) shouldBe true
    }

    @Test
    fun testRemove() {
        store.putString(KEY1, "value")
        store.remove(KEY1)
        store.getString(KEY1) shouldBe null

        store.putInt(KEY2, 567)
        store.remove(KEY2)
        store.getInt(KEY2) shouldBe 0

        store.putLong(KEY3, 888)
        store.remove(KEY3)
        store.getLong(KEY3) shouldBe 0

        store.putFloat(KEY4, 44.2f)
        store.remove(KEY4)
        Assert.assertEquals(0.0f, store.getFloat(KEY4), DELTA.toFloat())

        store.putDouble(KEY5, 120120.0301)
        store.remove(KEY5)
        Assert.assertEquals(0.0, store.getDouble(KEY5), DELTA)

        store.putBoolean(KEY6, true)
        store.remove(KEY6)
        store.getBoolean(KEY6) shouldBe false
    }

    @Test
    fun testRemove_shouldDoNothing_withNonExistentKeyValue() {
        store.putInt(KEY1, 70)
        store.putString(KEY2, "value")

        store.remove(KEY)

        store.size shouldBe 2
        store.getInt(KEY1) shouldBe 70
        store.getString(KEY2) shouldBe "value"
    }

    @Test
    fun testSize_shouldBe_0_afterClear() {
        store.putBoolean(KEY, true)
        store.clear()

        store.size shouldBe 0
    }

    @Test
    fun testSize_shouldBeInfluencedBy_put_remove() {
        store.putString(KEY1, "value")
        store.putLong(KEY2, 18)
        store.putBoolean(KEY3, true)

        store.size shouldBe 3

        store.remove(KEY2)

        store.size shouldBe 2
    }

    @Test
    fun testIsEmpty_shouldBeTrue_afterClear() {
        store.putBoolean(KEY, true)
        store.clear()

        store.isEmpty shouldBe true
    }

    @Test
    fun testIsEmpty_shouldBeFalse_whenContainsItems() {
        store.putInt("key2", 18)
        store.putInt("key3", 10)

        store.isEmpty shouldBe false
    }
}