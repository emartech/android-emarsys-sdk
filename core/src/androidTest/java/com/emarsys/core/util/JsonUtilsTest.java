package com.emarsys.core.util;

import android.os.Build;

import com.emarsys.testUtil.TimeoutUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonUtilsTest {

    public static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    public static final String KEY_3 = "key3";
    public static final String KEY_4 = "key4";
    public static final String KEY_5 = "key5";
    public static final String KEY_6 = "key6";
    public static final String KEY_7 = "key7";
    public static final String KEY_8 = "key8";
    public static final String KEY_9 = "key9";
    public static final String KEY_10 = "key10";
    public static final String KEY_11 = "key11";
    public static final String KEY_12 = "key12";

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testFromMap_mapShouldNotBeNull() {
        JsonUtils.fromMap(null);
    }

    @Test
    public void testFromMap_emptyMap() {
        Map<String, Object> input = provideNewMap();
        JSONObject expected = new JSONObject();
        JSONObject result = JsonUtils.fromMap(input);
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testFromMap_basicValues() throws JSONException {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put(KEY_1, 2);
        input.put(KEY_2, (short) 200);
        input.put(KEY_3, (byte) 212);
        input.put(KEY_4, 4L);
        input.put(KEY_5, 4.0f);
        input.put(KEY_6, 0.7d);
        input.put(KEY_7, true);
        input.put(KEY_8, false);
        input.put(KEY_9, "string");
        input.put(KEY_10, 'c');
        input.put(KEY_11, new JSONArray(Arrays.asList("a", 1, "b")));
        input.put(KEY_12, new JSONObject().put("a", "a").put("b", 2));

        JSONObject expected = new JSONObject()
                .put(KEY_1, 2)
                .put(KEY_2, (short) 200)
                .put(KEY_3, (byte) 212)
                .put(KEY_4, 4L)
                .put(KEY_5, 4.0f)
                .put(KEY_6, 0.7d)
                .put(KEY_7, true)
                .put(KEY_8, false)
                .put(KEY_9, "string")
                .put(KEY_10, "c")
                .put(KEY_11, new JSONArray(Arrays.asList("a", 1, "b")))
                .put(KEY_12, new JSONObject().put("a", "a").put("b", 2));

        JSONObject result = JsonUtils.fromMap(input);

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testFromMap_nestedMap() throws JSONException {

        Map<String, Object> doublyNested1 = new LinkedHashMap<>();
        doublyNested1.put("c", "cc");
        doublyNested1.put("d", 20);

        Map<String, String> doublyNested2 = new LinkedHashMap<>();
        doublyNested2.put("e", "ee");
        doublyNested2.put("f", "ff");

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("a", "a");
        nested.put("b", 2);
        nested.put("nested1", doublyNested1);
        nested.put("nested2", doublyNested2);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put(KEY_1, nested);
        input.put(KEY_2, false);

        JSONObject expected = new JSONObject()
                .put(KEY_1, new JSONObject()
                        .put("a", "a")
                        .put("b", 2)
                        .put("nested1", new JSONObject()
                                .put("c", "cc")
                                .put("d", 20))
                        .put("nested2", new JSONObject()
                                .put("e", "ee")
                                .put("f", "ff")))
                .put(KEY_2, false);

        JSONObject result = JsonUtils.fromMap(input);

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testFromMap_nestedArray_withInnerObjects() throws JSONException {

        Map<String, Object> deeplyNested = provideNewMap();
        deeplyNested.put(KEY_5, "value");

        Map<String, Object> nested1 = provideNewMap();
        nested1.put(KEY_2, 3);
        nested1.put(KEY_3, false);
        nested1.put(KEY_4, deeplyNested);

        Map<String, Object> nested2 = provideNewMap();
        nested2.put(KEY_6, 4);
        nested2.put(KEY_7, "meid");

        ArrayList<Object> list = new ArrayList<>();
        list.add(nested1);
        list.add(nested2);

        Map<String, Object> input = provideNewMap();
        input.put(KEY_1, list);

        JSONObject expected = new JSONObject()
                .put(KEY_1,
                        new JSONArray()
                                .put(new JSONObject()
                                        .put(KEY_2, 3)
                                        .put(KEY_3, false)
                                        .put(KEY_4, new JSONObject()
                                                .put(KEY_5, "value")))
                                .put(new JSONObject()
                                        .put(KEY_6, 4)
                                        .put(KEY_7, "meid")));

        JSONObject result = JsonUtils.fromMap(input);

        assertEquals(expected.toString(), result.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromList_shouldNotAcceptNull() {
        JsonUtils.fromList(null);
    }

    @Test
    public void testFromList_basicValues() throws JSONException {
        ArrayList<Object> input = new ArrayList<>();
        input.add(2);
        input.add((short) 200);
        input.add((byte) 212);
        input.add(4L);
        input.add(4.0f);
        input.add(0.7d);
        input.add(true);
        input.add(false);
        input.add("string");
        input.add('c');
        input.add(new JSONArray(Arrays.asList("a", 1, "b")));
        input.add(new JSONObject().put("a", "a").put("b", 2));

        JSONArray expected = new JSONArray()
                .put(2)
                .put((short) 200)
                .put((byte) 212)
                .put(4L)
                .put(4.0f)
                .put(0.7d)
                .put(true)
                .put(false)
                .put("string")
                .put("c")
                .put(new JSONArray(Arrays.asList("a", 1, "b")))
                .put(new JSONObject().put("a", "a").put("b", 2));

        JSONArray result = JsonUtils.fromList(input);

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testFromList_nestedArray() {

        List<Object> nested = new ArrayList<>();
        nested.add("a");
        nested.add("b");
        nested.add("c");

        List<Object> input = new ArrayList<>();
        input.add(1);
        input.add(4);
        input.add(false);
        input.add(nested);
        input.add("end");

        JSONArray expected = new JSONArray()
                .put(1)
                .put(4)
                .put(false)
                .put(new JSONArray()
                        .put("a")
                        .put("b")
                        .put("c"))
                .put("end");

        JSONArray result = JsonUtils.fromList(input);

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testFromList_withNestedObjects() throws JSONException {
        Map<String, Object> nested1 = provideNewMap();
        nested1.put(KEY_1, "a");
        nested1.put(KEY_2, 543);

        Map<String, Object> deeplyNested1 = provideNewMap();
        deeplyNested1.put(KEY_4, false);
        deeplyNested1.put(KEY_5, 0.4);

        Map<String, Object> deeplyNested2 = provideNewMap();
        deeplyNested2.put(KEY_7, "id");
        deeplyNested2.put(KEY_8, 123456789);

        Map<String, Object> nested2 = provideNewMap();
        nested2.put(KEY_3, deeplyNested1);
        nested2.put(KEY_6, deeplyNested2);

        List<Object> input = new ArrayList<>();
        input.add(nested1);
        input.add("___");
        input.add(nested2);

        JSONArray expected = new JSONArray()
                .put(new JSONObject()
                        .put(KEY_1, "a")
                        .put(KEY_2, 543))
                .put("___")
                .put(new JSONObject()
                        .put(KEY_3, new JSONObject()
                                .put(KEY_4, false)
                                .put(KEY_5, 0.4))
                        .put(KEY_6, new JSONObject()
                                .put(KEY_7, "id")
                                .put(KEY_8, 123456789))
                );

        JSONArray result = JsonUtils.fromList(input);

        assertEquals(expected.toString(), result.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMerge_throwException_ifArgumentListIsEmpty() {
        JsonUtils.merge();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMerge_throwException_ifEveryArgumentIsNull() {
        JsonUtils.merge(null, null, null);
    }

    @Test
    public void testMerge_withOnlyOneElement() throws JSONException {
        JSONObject expected = new JSONObject().put("key", "value");
        JSONObject actual = JsonUtils.merge(expected);

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testMerge_withMultipleEmptyJsonElements() throws JSONException {
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();

        JSONObject actual = JsonUtils.merge(json1, json2, json3);

        JSONObject expected = new JSONObject();

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testMerge_withMultipleElements() throws JSONException {
        JSONObject json1 = new JSONObject()
                .put("key1", "value1")
                .put("key2", "value2");
        JSONObject json2 = new JSONObject()
                .put("key2", true);
        JSONObject json3 = new JSONObject();
        JSONObject json4 = new JSONObject()
                .put("key3",
                        new JSONObject().put("nestedKey", 567));

        JSONObject expected = new JSONObject()
                .put("key1", "value1")
                .put("key2", true)
                .put("key3",
                        new JSONObject().put("nestedKey", 567));

        JSONObject actual = JsonUtils.merge(json1, json2, json3, json4);

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testMerge_withOneNullAndMultipleNotNullElements() throws JSONException {
        JSONObject json1 = new JSONObject()
                .put("key1", "value1")
                .put("key2", "value2");
        JSONObject json2 = new JSONObject()
                .put("key3", true);

        JSONObject actual = JsonUtils.merge(json1, json2, null);

        JSONObject expected = new JSONObject()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", true);

        assertEquals(expected.toString(), actual.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToFlatMap_shouldNotAcceptNull() {
        JsonUtils.toFlatMap(null);
    }

    @Test
    public void testToFloatMap_withEmptyJsonObject() {
        Map<String, String> result = JsonUtils.toFlatMap(new JSONObject());
        Map<String, String> expected = new HashMap<>();

        assertEquals(expected, result);
    }

    @Test
    public void testToFloatMap_withJsonObjectOfStringValues() throws JSONException {
        JSONObject input = new JSONObject()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", "value3");
        Map<String, String> result = JsonUtils.toFlatMap(input);

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");

        assertEquals(expected, result);
    }

    @Test
    public void testToFloatMap_withJsonObjectOfStringValuesAndNull() throws JSONException {
        JSONObject input = new JSONObject()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", null)
                .put("key4", new JSONObject()
                        .put("nestedKey1", "nestedValue1")
                        .put("nestedKey2", null));
        Map<String, String> result = JsonUtils.toFlatMap(input);

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key4", "{\"nestedKey1\":\"nestedValue1\"}");

        assertEquals(expected, result);
    }

    @Test
    public void testToFloatMap_withJsonObjectOfMixedValues() throws JSONException {
        JSONObject input = new JSONObject()
                .put("key1", "value1")
                .put("key2", 3.14)
                .put("key3", false)
                .put("key4", new JSONObject()
                        .put("nestedKey1", "nestedValue1")
                        .put("nestedKey2", 900));
        Map<String, String> result = JsonUtils.toFlatMap(input);

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "3.14");
        expected.put("key3", "false");
        expected.put("key4", "{\"nestedKey1\":\"nestedValue1\",\"nestedKey2\":900}");

        assertEquals(expected, result);
    }

    private Map<String, Object> provideNewMap() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return new HashMap<>();
        } else {
            return new LinkedHashMap<>();
        }
    }
}