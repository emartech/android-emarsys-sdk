package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.collection.ArraySet
import com.emarsys.core.util.tryCastOrNull
import com.google.crypto.tink.Aead
import com.google.crypto.tink.DeterministicAead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.daead.DeterministicAeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.subtle.Base64
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class EmarsysSecureSharedPreferences private constructor(
        private val fileName: String,
        private val sharedPreferences: SharedPreferences,
        private val aead: Aead,
        private val deterministicAead: DeterministicAead,
        private val listeners: MutableList<OnSharedPreferenceChangeListener> = mutableListOf()) : SharedPreferences {

    companion object {
        private const val KEY_KEYSET_ALIAS = "__emarsys_encrypted_prefs_key_keyset__"
        private const val VALUE_KEYSET_ALIAS = "__emarsys_encrypted_prefs_value_keyset__"
        private const val NULL_VALUE = "__NULL__";

        fun create(fileName: String,
                   context: Context): SharedPreferences {
            DeterministicAeadConfig.register()
            AeadConfig.register()

            val applicationContext = context.applicationContext
            val daeadKeysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
                    .withKeyTemplate(KeyTemplates.get("AES256_SIV"))
                    .withSharedPref(applicationContext, KEY_KEYSET_ALIAS, fileName)
                    .build().keysetHandle
            val aeadKeysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    .withSharedPref(applicationContext, VALUE_KEYSET_ALIAS, fileName)
                    .build().keysetHandle
            val daead: DeterministicAead = daeadKeysetHandle.getPrimitive(DeterministicAead::class.java)
            val aead: Aead = aeadKeysetHandle.getPrimitive(Aead::class.java)
            return EmarsysSecureSharedPreferences(fileName,
                    applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE),
                    aead,
                    daead)
        }
    }

    private class Editor(private val encryptedSharedPreferences: EmarsysSecureSharedPreferences,
                         private val editor: SharedPreferences.Editor) : SharedPreferences.Editor {
        private val keysChanged: MutableList<String?> = CopyOnWriteArrayList()
        private val clearRequested = AtomicBoolean(false)

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            var mutableValue = value
            if (mutableValue == null) {
                mutableValue = NULL_VALUE
            }
            val stringBytes = mutableValue.toByteArray(StandardCharsets.UTF_8)
            val stringByteLength = stringBytes.size
            ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + stringByteLength).also {
                it.putInt(EncryptedType.STRING.id)
                it.putInt(stringByteLength)
                it.put(stringBytes)
                putEncryptedObject(key, it.array())
            }
            return this
        }

        override fun putStringSet(key: String,
                                  values: Set<String>?): SharedPreferences.Editor {
            val mutableValues = values ?: mutableSetOf(NULL_VALUE)

            var totalBytes = mutableValues.size * Integer.BYTES + Integer.BYTES
            val byteValues = mutableValues.map { value ->
                value.toByteArray(StandardCharsets.UTF_8).also { totalBytes += it.size }
            }
            ByteBuffer.allocate(totalBytes).also {
                it.putInt(EncryptedType.STRING_SET.id)
                for (bytes in byteValues) {
                    it.putInt(bytes.size)
                    it.put(bytes)
                }
                putEncryptedObject(key, it.array())
            }
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            ByteBuffer.allocate(Integer.BYTES + Integer.BYTES).also {
                it.putInt(EncryptedType.INT.id)
                it.putInt(value)
                putEncryptedObject(key, it.array())
            }
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            ByteBuffer.allocate(Integer.BYTES + java.lang.Long.BYTES).also {
                it.putInt(EncryptedType.LONG.id)
                it.putLong(value)
                putEncryptedObject(key, it.array())
            }
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            ByteBuffer.allocate(Integer.BYTES + java.lang.Float.BYTES).also {
                it.putInt(EncryptedType.FLOAT.id)
                it.putFloat(value)
                putEncryptedObject(key, it.array())
            }
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            ByteBuffer.allocate(Integer.BYTES + java.lang.Byte.BYTES).also {
                it.putInt(EncryptedType.BOOLEAN.id)
                it.put(if (value) 1.toByte() else 0.toByte())
                putEncryptedObject(key, it.array())
            }
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            if (encryptedSharedPreferences.isReservedKey(key)) {
                throw SecurityException("$key is a reserved key for the encryption keyset.")
            }
            editor.remove(encryptedSharedPreferences.encryptKey(key))
            keysChanged.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearRequested.set(true)
            return this
        }

        override fun commit(): Boolean {
            clearKeysIfNeeded()
            return try {
                editor.commit()
            } finally {
                notifyListeners()
                keysChanged.clear()
            }
        }

        override fun apply() {
            clearKeysIfNeeded()
            editor.apply()
            notifyListeners()
            keysChanged.clear()
        }

        private fun clearKeysIfNeeded() {
            if (clearRequested.getAndSet(false)) {
                encryptedSharedPreferences.all.keys.filter {
                    !keysChanged.contains(it)
                            && encryptedSharedPreferences.isReservedKey(it)
                }.forEach {
                    editor.remove(encryptedSharedPreferences.encryptKey(it))
                }
            }
        }

        private fun putEncryptedObject(key: String, value: ByteArray) {
            if (encryptedSharedPreferences.isReservedKey(key)) {
                throw SecurityException("$key is a reserved key for the encryption keyset.")
            }
            keysChanged.add(key)
            try {
                val encryptedPair = encryptedSharedPreferences.encryptKeyValuePair(key, value)
                editor.putString(encryptedPair.first, encryptedPair.second)
            } catch (ex: GeneralSecurityException) {
                throw SecurityException("Could not encrypt data: " + ex.message, ex)
            }
        }

        private fun notifyListeners() {
            encryptedSharedPreferences.listeners.forEach { listener ->
                keysChanged.forEach { key ->
                    listener.onSharedPreferenceChanged(encryptedSharedPreferences, key)
                }
            }
        }
    }

    override fun getAll(): Map<String, Any?> {
        return sharedPreferences.all.entries.filter {
            !isReservedKey(it.key)
        }.associate {
            val decryptedKey = decryptKey(it.key)
            Pair(decryptedKey, getDecryptedObject(decryptedKey))

        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return getDecryptedObject(key) ?: defValue
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        val returnValues: Set<String>?
        val value: Set<*>? = getDecryptedObject(key)
        returnValues = value?.tryCastOrNull<Set<String>>()

        return returnValues ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return getDecryptedObject(key) ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return getDecryptedObject(key) ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return getDecryptedObject(key) ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return getDecryptedObject(key) ?: defValue
    }

    override fun contains(key: String): Boolean {
        if (isReservedKey(key)) {
            throw SecurityException("$key is a reserved key for the encryption keyset.")
        }
        val encryptedKey = encryptKey(key)
        return sharedPreferences.contains(encryptedKey)
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor(this, sharedPreferences.edit())
    }

    override fun registerOnSharedPreferenceChangeListener(
            listener: OnSharedPreferenceChangeListener) {
        listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(
            listener: OnSharedPreferenceChangeListener) {
        listeners.remove(listener)
    }

    private enum class EncryptedType(val id: Int) {
        STRING(0),
        STRING_SET(1),
        INT(2),
        LONG(3),
        FLOAT(4),
        BOOLEAN(5);

        companion object {
            fun fromId(id: Int): EncryptedType? {
                when (id) {
                    0 -> return STRING
                    1 -> return STRING_SET
                    2 -> return INT
                    3 -> return LONG
                    4 -> return FLOAT
                    5 -> return BOOLEAN
                }
                return null
            }
        }
    }

    private inline fun <reified T> getDecryptedObject(key: String): T? {
        if (isReservedKey(key)) {
            throw SecurityException("$key is a reserved key for the encryption keyset.")
        }
        var returnValue: Any? = null
        try {
            val encryptedKey = encryptKey(key)
            val encryptedValue: String? = sharedPreferences.getString(encryptedKey, null)
            if (encryptedValue != null) {
                val cipherText = Base64.decode(encryptedValue, Base64.DEFAULT)
                val value: ByteArray = aead.decrypt(cipherText, encryptedKey.toByteArray(StandardCharsets.UTF_8))
                val buffer = ByteBuffer.wrap(value)
                buffer.position(0)
                val typeId = buffer.int
                when (EncryptedType.fromId(typeId)) {
                    EncryptedType.STRING -> {
                        val stringLength = buffer.int
                        val stringSlice = buffer.slice()
                        buffer.limit(stringLength)
                        val stringValue = StandardCharsets.UTF_8.decode(stringSlice).toString()
                        returnValue = if (stringValue == NULL_VALUE) {
                            null
                        } else {
                            stringValue
                        }
                    }
                    EncryptedType.INT -> returnValue = buffer.int
                    EncryptedType.LONG -> returnValue = buffer.long
                    EncryptedType.FLOAT -> returnValue = buffer.float
                    EncryptedType.BOOLEAN -> returnValue = buffer.get() != 0.toByte()
                    EncryptedType.STRING_SET -> {
                        val stringSet = ArraySet<String>()
                        while (buffer.hasRemaining()) {
                            val subStringLength = buffer.int
                            val subStringSlice = buffer.slice()
                            subStringSlice.limit(subStringLength)
                            buffer.position(buffer.position() + subStringLength)
                            stringSet.add(StandardCharsets.UTF_8.decode(subStringSlice).toString())
                        }
                        returnValue =
                            if (stringSet.size == 1 && NULL_VALUE == stringSet.valueAt(0)) {
                                null
                            } else {
                                stringSet
                            }
                    }
                    else -> {}
                }
            }
        } catch (ex: GeneralSecurityException) {
            throw SecurityException("Could not decrypt value. " + ex.message, ex)
        }
        return returnValue?.tryCastOrNull<T>()
    }

    fun encryptKey(key: String): String {
        return try {
            val encryptedKeyBytes: ByteArray = deterministicAead.encryptDeterministically(
                    key.toByteArray(StandardCharsets.UTF_8),
                    fileName.toByteArray())
            Base64.encode(encryptedKeyBytes)
        } catch (ex: GeneralSecurityException) {
            throw SecurityException("Could not encrypt key. " + ex.message, ex)
        }
    }

    private fun decryptKey(encryptedKey: String): String {
        return try {
            val clearText: ByteArray = deterministicAead.decryptDeterministically(
                    Base64.decode(encryptedKey, Base64.DEFAULT),
                    fileName.toByteArray())
            String(clearText, StandardCharsets.UTF_8)
        } catch (ex: GeneralSecurityException) {
            throw SecurityException("Could not decrypt key. " + ex.message, ex)
        }
    }

    fun isReservedKey(key: String): Boolean {
        return KEY_KEYSET_ALIAS == key || VALUE_KEYSET_ALIAS == key
    }

    fun encryptKeyValuePair(key: String, value: ByteArray?): Pair<String, String?> {
        val encryptedKey = encryptKey(key)
        val cipherText: ByteArray = aead.encrypt(value, encryptedKey.toByteArray(StandardCharsets.UTF_8))
        return Pair(encryptedKey, Base64.encode(cipherText))
    }
}
