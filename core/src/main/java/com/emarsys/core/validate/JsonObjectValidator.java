package com.emarsys.core.validate;

import com.emarsys.core.util.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonObjectValidator {

    private final JSONObject json;
    private final List<String> errors;

    public static JsonObjectValidator from(JSONObject jsonObject) {
        Assert.notNull(jsonObject, "JsonObject must not be null!");
        return new JsonObjectValidator(jsonObject);
    }

    private JsonObjectValidator(JSONObject json) {
        this.json = json;
        this.errors = new ArrayList<>();
    }

    public JsonObjectValidator hasField(String fieldName) {
        Assert.notNull(fieldName, "FieldName must not be null!");
        if (!json.has(fieldName)) {
            errors.add(String.format("Missing field: '%s'", fieldName));
        }
        return this;
    }

    public JsonObjectValidator hasFieldWithType(String fieldName, Class fieldType) {
        Assert.notNull(fieldName, "FieldName must not be null!");
        Assert.notNull(fieldType, "FieldType must not be null!");

        if (!json.has(fieldName)) {
            errors.add(String.format("Missing field: '%s' with type: %s", fieldName, fieldType));
        } else {
            try {
                Object value = json.get(fieldName);
                if (fieldType != value.getClass()) {
                    errors.add(String.format("Type mismatch for key: '%s', expected type: %s, but was: %s", fieldName, fieldType, value.getClass()));
                }

            } catch (JSONException ignore) {
            }
        }

        return this;
    }

    public List<String> validate() {
        return errors;
    }

}
