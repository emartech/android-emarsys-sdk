package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.crypto.SharedPreferenceCrypto
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.crypto.SecretKey

class EmarsysEncryptedSharedPreferencesV3Test : AnnotationSpec() {

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferenceCrypto: SharedPreferenceCrypto
    private lateinit var mockRealPreferences: SharedPreferences
    private lateinit var mockSecretKey: SecretKey
    private lateinit var emarsysEncryptedSharedPreferencesV3: EmarsysEncryptedSharedPreferencesV3
    private lateinit var mockInternalEditor: SharedPreferences.Editor

    @BeforeEach
    fun setup() {
        mockContext = mockk()
        mockSharedPreferenceCrypto = mockk()
        mockRealPreferences = mockk(relaxed = true)
        mockSecretKey = mockk()

        every { mockContext.getSharedPreferences(any(), any()) } returns mockRealPreferences
        every { mockSharedPreferenceCrypto.getOrCreateSecretKey() } returns mockSecretKey
        mockInternalEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { mockRealPreferences.edit() } returns mockInternalEditor
        every { mockSharedPreferenceCrypto.encrypt(any(), any()) } returns "encryptedValue"

        emarsysEncryptedSharedPreferencesV3 = EmarsysEncryptedSharedPreferencesV3(
            mockContext,
            "test_file",
            mockSharedPreferenceCrypto
        )
    }

    @Test
    fun testGetAll() {
        val encryptedMap = mapOf(
            "key1" to "encryptedValue1",
            "key2" to 42,
            "key3" to true,
            "key4" to 3.14f,
            "key5" to 1234L,
            "key6" to setOf("encryptedValue2", "encryptedValue3")
        )
        every { mockRealPreferences.all } returns encryptedMap
        every {
            mockSharedPreferenceCrypto.decrypt(
                any(),
                any()
            )
        } returnsMany listOf("decryptedValue1", "decryptedValue2", "decryptedValue3")

        val result = emarsysEncryptedSharedPreferencesV3.getAll()

        result shouldBe mapOf(
            "key1" to "decryptedValue1",
            "key2" to 42,
            "key3" to true,
            "key4" to 3.14f,
            "key5" to 1234L,
            "key6" to setOf("decryptedValue2", "decryptedValue3")
        )
    }

    @Test
    fun testGetString() {
        every { mockRealPreferences.getString("testKey", null) } returns "encryptedValue"
        every {
            mockSharedPreferenceCrypto.decrypt(
                "encryptedValue",
                mockSecretKey
            )
        } returns "decryptedValue"

        val result = emarsysEncryptedSharedPreferencesV3.getString("testKey", "defaultValue")

        result shouldBe "decryptedValue"
    }

    @Test
    fun testGetStringSet() {
        val encryptedSet = setOf("encryptedValue1", "encryptedValue2")
        every { mockRealPreferences.getStringSet("testKey", null) } returns encryptedSet
        every {
            mockSharedPreferenceCrypto.decrypt(
                "encryptedValue1",
                mockSecretKey
            )
        } returns "decryptedValue1"
        every {
            mockSharedPreferenceCrypto.decrypt(
                "encryptedValue2",
                mockSecretKey
            )
        } returns "decryptedValue2"

        val result = emarsysEncryptedSharedPreferencesV3.getStringSet("testKey", mutableSetOf())

        result shouldBe mutableSetOf("decryptedValue1", "decryptedValue2")
    }

    @Test
    fun testGetInt() {
        every { mockRealPreferences.getInt("testKey", 0) } returns 42

        val result = emarsysEncryptedSharedPreferencesV3.getInt("testKey", 0)

        result shouldBe 42
    }

    @Test
    fun testGetLong() {
        every { mockRealPreferences.getLong("testKey", 0L) } returns 1234L

        val result = emarsysEncryptedSharedPreferencesV3.getLong("testKey", 0L)

        result shouldBe 1234L
    }

    @Test
    fun testGetFloat() {
        every { mockRealPreferences.getFloat("testKey", 0f) } returns 3.14f

        val result = emarsysEncryptedSharedPreferencesV3.getFloat("testKey", 0f)

        result shouldBe 3.14f
    }

    @Test
    fun testGetBoolean() {
        every { mockRealPreferences.getBoolean("testKey", false) } returns true

        val result = emarsysEncryptedSharedPreferencesV3.getBoolean("testKey", false)

        result shouldBe true
    }

    @Test
    fun testContains() {
        every { mockRealPreferences.contains("testKey") } returns true

        val result = emarsysEncryptedSharedPreferencesV3.contains("testKey")

        result shouldBe true
    }

    @Test
    fun testEdit() {
        every { mockRealPreferences.edit() } returns mockInternalEditor

        val editor = emarsysEncryptedSharedPreferencesV3.edit()

        editor.putString("testKey", "testValue")
        editor.putInt("testIntKey", 42)
        editor.putBoolean("testBoolKey", true)
        editor.putFloat("testFloatKey", 3.14f)
        editor.putLong("testLongKey", 1234L)
        editor.putStringSet("testSetKey", mutableSetOf("value1", "value2"))

        editor.commit()

        verify(exactly = 1) { mockSharedPreferenceCrypto.encrypt("testValue", mockSecretKey) }
        verify(exactly = 1) { mockSharedPreferenceCrypto.encrypt("value1", mockSecretKey) }
        verify(exactly = 1) { mockSharedPreferenceCrypto.encrypt("value2", mockSecretKey) }

        verify(exactly = 1) { mockInternalEditor.putString("testKey", "encryptedValue") }
        verify(exactly = 1) { mockInternalEditor.putInt("testIntKey", 42) }
        verify(exactly = 1) { mockInternalEditor.putBoolean("testBoolKey", true) }
        verify(exactly = 1) { mockInternalEditor.putFloat("testFloatKey", 3.14f) }
        verify(exactly = 1) { mockInternalEditor.putLong("testLongKey", 1234L) }
        verify(exactly = 1) {
            mockInternalEditor.putStringSet(
                "testSetKey",
                setOf("encryptedValue", "encryptedValue")
            )
        }
        verify(exactly = 1) { mockInternalEditor.commit() }
    }

    @Test
    fun testRegisterAndUnregisterOnSharedPreferenceChangeListener() {
        val listener: SharedPreferences.OnSharedPreferenceChangeListener = mockk()

        emarsysEncryptedSharedPreferencesV3.registerOnSharedPreferenceChangeListener(listener)
        verify(exactly = 1) { mockRealPreferences.registerOnSharedPreferenceChangeListener(listener) }

        emarsysEncryptedSharedPreferencesV3.unregisterOnSharedPreferenceChangeListener(listener)
        verify(exactly = 1) {
            mockRealPreferences.unregisterOnSharedPreferenceChangeListener(
                listener
            )
        }
    }
}