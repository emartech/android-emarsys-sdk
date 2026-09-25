package com.emarsys.core.validate

import org.json.JSONException
import org.json.JSONObject

class JsonObjectValidator private constructor(private val json: JSONObject) {

    private val errors: MutableList<String> = mutableListOf()

    companion object {
        fun from(jsonObject: JSONObject): JsonObjectValidator = JsonObjectValidator(jsonObject)
    }

    fun hasField(fieldName: String): JsonObjectValidator {
        if (!json.has(fieldName)) {
            errors.add("Missing field: '$fieldName'")
        }
        return this
    }

    fun hasFieldWithType(fieldName: String, fieldType: Class<*>): JsonObjectValidator {
        if (!json.has(fieldName)) {
            errors.add("Missing field: '$fieldName' with type: $fieldType")
        } else {
            try {
                val value = json.get(fieldName)
                if (fieldType != value.javaClass) {
                    errors.add("Type mismatch for key: '$fieldName', expected type: $fieldType, but was: ${value.javaClass}")
                }
            } catch (ignored: JSONException) {
            }
        }
        return this
    }

    fun validate(): List<String> = errors
}
