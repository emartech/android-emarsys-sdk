package com.emarsys.core.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    public static JSONObject fromMap(Map<String, Object> map) {
        Assert.notNull(map, "Map must not be null!");

        JSONObject result = new JSONObject();

        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Map) {
                    result.put(key, JsonUtils.fromMap((Map<String, Object>) value));
                } else if (value instanceof List) {
                    result.put(key, JsonUtils.fromList((List<Object>) value));
                } else {
                    result.put(key, value);
                }
            }
        } catch (JSONException je) {
            throw new IllegalArgumentException(je);
        }

        return result;
    }

    public static JSONArray fromList(List<Object> list) {
        Assert.notNull(list, "List must not be null!");

        JSONArray result = new JSONArray();

        for (Object item : list) {
            if (item instanceof List) {
                result.put(JsonUtils.fromList((List<Object>) item));
            } else if (item instanceof Map) {
                result.put(JsonUtils.fromMap((Map<String, Object>) item));
            } else {
                result.put(item);
            }
        }

        return result;
    }

    public static JSONObject merge(JSONObject... jsonObjects) {
        validateArgument(jsonObjects);

        JSONObject result = new JSONObject();

        for (JSONObject jsonObject : jsonObjects) {
            if (jsonObject != null) {
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        result.put(key, jsonObject.get(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public static Map<String, String> toFlatMap(JSONObject jsonObject) {
        Assert.notNull(jsonObject, "JsonObject must not be null!");

        Map<String, String> result = new HashMap<>();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            try {
                String key = iterator.next();
                String value = jsonObject.getString(key);
                result.put(key, value);
            } catch (JSONException ignore) {
            }
        }
        return result;
    }

    private static void validateArgument(JSONObject[] jsonObjects) {
        if (jsonObjects.length == 0) {
            throw new IllegalArgumentException("Argument must not be empty array!");
        }

        int nullCount = 0;
        for (JSONObject jsonObject : jsonObjects) {
            if (jsonObject == null) {
                nullCount++;
            }
        }
        if (nullCount == jsonObjects.length) {
            throw new IllegalArgumentException("Argument must contain at least one not null element!");
        }
    }

}