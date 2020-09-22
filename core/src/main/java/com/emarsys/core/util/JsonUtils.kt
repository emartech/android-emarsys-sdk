package com.emarsys.core.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object JsonUtils {
    @JvmStatic
    fun fromMap(map: Map<String, Any>): JSONObject {
        val result = JSONObject()
        try {
            for ((key, value) in map) {
                when (value) {
                    is Map<*, *> -> {
                        result.put(key, fromMap(value as Map<String, Any>))
                    }
                    is List<*> -> {
                        result.put(key, fromList(value as List<Any?>))
                    }
                    else -> {
                        result.put(key, value)
                    }
                }
            }
        } catch (je: JSONException) {
            throw IllegalArgumentException(je)
        }
        return result
    }

    @JvmStatic
    fun fromList(list: List<Any?>): JSONArray {
        val result = JSONArray()
        for (item in list) {
            when (item) {
                is List<Any?> -> {
                    result.put(fromList(item))
                }
                is Map<*, *> -> {
                    result.put(fromMap(item as Map<String, Any>))
                }
                else -> {
                    result.put(item)
                }
            }
        }
        return result
    }

    @JvmStatic
    fun merge(vararg jsonObjects: JSONObject?): JSONObject {
        validateArgument(arrayOf(*jsonObjects))

        val result = JSONObject()
        for (jsonObject in jsonObjects) {
            jsonObject?.keys()?.asSequence()?.forEach {
                try {
                    result.put(it, jsonObject[it])
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    @JvmStatic
    fun toFlatMap(jsonObject: JSONObject): Map<String, String> {
        val result: MutableMap<String, String> = HashMap()
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            try {
                val key = iterator.next()
                val value = jsonObject.getString(key).convertNullStringValueToNull()
                if (value != null) {
                    result[key] = value
                }
            } catch (ignore: JSONException) {
            }
        }
        return result
    }

    private fun validateArgument(jsonObjects: Array<JSONObject?>) {
        require(jsonObjects.isNotEmpty()) { "Argument must not be empty array!" }
        var nullCount = 0
        for (jsonObject in jsonObjects) {
            if (jsonObject == null) {
                nullCount++
            }
        }
        require(nullCount != jsonObjects.size) { "Argument must contain at least one not null element!" }
    }
}

private fun String.convertNullStringValueToNull(): String? {
    return if (this == "null") null else this
}
