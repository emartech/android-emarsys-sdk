package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.crypto.SharedPreferenceCrypto

class EmarsysEncryptedSharedPreferencesV3(
    context: Context,
    fileName: String,
    private val sharedPreferenceCrypto: SharedPreferenceCrypto
) : SharedPreferences {

    private val realPreferences: SharedPreferences =
        context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = Editor(
        realPreferences,
        sharedPreferenceCrypto
    )

    override fun getAll(): Map<String, *> {
        val encryptedData = realPreferences.all
        val decryptedData = mutableMapOf<String, Any?>()
        for ((key, value) in encryptedData) {
            when (value) {
                is String -> decryptedData[key] = decryptString(value)
                is Int -> decryptedData[key] = value
                is Boolean -> decryptedData[key] = value
                is Float -> decryptedData[key] = value
                is Long -> decryptedData[key] = value
                is Set<*> -> decryptedData[key] =
                    (value as Set<String>).map { decryptString(it) }.toSet()
            }
        }
        return decryptedData
    }

    override fun getString(key: String, defValue: String?): String? {
        val encryptedValue = realPreferences.getString(key, null)
        return encryptedValue?.let { decryptString(it) } ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        val encryptedValue = realPreferences.getStringSet(key, null)
        return encryptedValue?.mapNotNull { decryptString(it) }?.toMutableSet() ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return realPreferences.getInt(key, defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return realPreferences.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return realPreferences.getFloat(key, defValue)
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return realPreferences.getBoolean(key, defValue)
    }

    override fun contains(key: String?): Boolean {
        return realPreferences.contains(key)
    }

    override fun edit(): SharedPreferences.Editor {
        return editor
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        realPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        realPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun decryptString(value: String): String? {
        return sharedPreferenceCrypto.decrypt(value)
    }

    private class Editor(
        realPreferences: SharedPreferences,
        private val sharedPreferenceCrypto: SharedPreferenceCrypto
    ) : SharedPreferences.Editor {
        private val editor: SharedPreferences.Editor = realPreferences.edit()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            editor.putString(key, encryptString(value))
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            editor.putInt(key, value)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            editor.putBoolean(key, value)
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            editor.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            editor.clear()
            return this
        }

        override fun commit(): Boolean {
            return editor.commit()
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            editor.putFloat(key, value)
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            editor.putLong(key, value)
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            editor
                .putStringSet(key, values?.map { encryptString(it) }?.toMutableSet())

            return this
        }

        override fun apply() {
            editor.apply()
        }

        private fun encryptString(value: String?): String? {
            value ?: return null
            return sharedPreferenceCrypto.encrypt(value)
        }
    }
}
