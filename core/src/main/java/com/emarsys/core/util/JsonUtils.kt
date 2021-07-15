package com.emarsys.core.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonUtils {

    fun JSONArray.toMutableList(): MutableList<JSONObject> = MutableList(length(), this::getJSONObject)

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
        return jsonObject.keys().asSequence().associateBy({ key -> key }) { key ->
            try {
                jsonObject.getString(key).convertNullStringValueToNull()
            } catch (ignore: JSONException) {
                null
            }
        }.mapNotNull { it.value?.let { value -> it.key to value } }.toMap()
    }

    @JvmStatic
    fun toMap(jsonObject: JSONObject): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        jsonObject.keys().asSequence().forEach {
            when (val value = jsonObject.get(it)) {
                is JSONObject -> {
                    result[it] = toMap(value)
                }
                is JSONArray -> {
                    result[it] = toList(value)
                }
                else -> {
                    result[it] = value
                }
            }
        }
        return result
    }

    @JvmStatic
    fun toList(jsonArray: JSONArray): List<Any> {
        val result = mutableListOf<Any>()
        for (i in 0 until jsonArray.length()) {
            when (val value = jsonArray.get(i)) {
                is JSONObject -> {
                    result.add(toMap(value))
                }
                is JSONArray -> {
                    result.add(toList(value))
                }
                else -> {
                    result.add(value)
                }
            }
        }
        return result
    }

    @JvmStatic
    fun toFlatMapIncludingNulls(jsonObject: JSONObject): Map<String, String?> {
        return jsonObject.keys().asSequence().associateBy({ key -> key }) { key ->
            try {
                jsonObject.getString(key).convertNullStringValueToNull()
            } catch (ignore: JSONException) {
                null
            }
        }
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

fun JSONObject.getNullableString(key: String): String? {
    return if (this.isNull(key)) null else this.getString(key)
}

fun JSONObject.getNullableLong(key: String): Long? {
    return if (this.isNull(key)) null else this.getLong(key)
}

private fun String.convertNullStringValueToNull(): String? {
    return if (this == "null") null else this
}
