package com.emarsys.core.storage

interface KeyValueStore {
    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
    fun putLong(key: String, value: Long)
    fun putFloat(key: String, value: Float)
    fun putDouble(key: String, value: Double)
    fun putBoolean(key: String, value: Boolean)
    fun getString(key: String): String?
    fun getInt(key: String): Int
    fun getLong(key: String): Long
    fun getFloat(key: String): Float
    fun getDouble(key: String): Double
    fun getBoolean(key: String): Boolean
    fun remove(key: String)
    fun clear()
    val size: Int
    val isEmpty: Boolean
}
