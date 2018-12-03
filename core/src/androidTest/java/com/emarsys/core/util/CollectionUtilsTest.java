package com.emarsys.core.util;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CollectionUtilsTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMergeMaps_mustProvideAtLeastOneArgument() {
        CollectionUtils.mergeMaps();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeMaps_mapsMustNotBeNull() {
        CollectionUtils.mergeMaps((Map[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeMaps_mapsMustNotContainNullValue() {
        CollectionUtils.mergeMaps(null, mock(Map.class), null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeMaps_withSingleInput() {
        Map<Integer, String> input = new HashMap<>();
        input.put(20, "A");
        input.put(30, "B");
        input.put(40, "C");

        Map<Integer, String> expected = new HashMap<>(input);

        Map<Integer, String> actual = CollectionUtils.mergeMaps(input);

        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeMaps_withMultipleInput() {
        Map<String, Integer> input1 = new HashMap<>();
        input1.put("A", 20);
        input1.put("B", 30);
        input1.put("C", 40);

        Map<String, Integer> input2 = new HashMap<>();
        input2.put("D", 21);
        input2.put("E", 31);
        input2.put("F", 41);

        Map<String, Integer> input3 = new HashMap<>();
        input3.put("B", 24);
        input3.put("E", 34);
        input3.put("N", 44);

        Map<String, Integer> expected = new HashMap<>();
        expected.putAll(input1);
        expected.putAll(input2);
        expected.putAll(input3);

        Map<String, Integer> actual = CollectionUtils.mergeMaps(input1, input2, input3);

        assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMergeLists_mustProvideAtLeastOneArgument() {
        CollectionUtils.mergeLists();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeLists_listsMustNotBeNull() {
        CollectionUtils.mergeLists((List[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeLists_listsMustNotContainNullValue() {
        CollectionUtils.mergeLists(null, mock(List.class), null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeLists_withMultipleInput() {
        List<String> input1 = new ArrayList<>();
        input1.add("A");
        input1.add("B");
        input1.add("C");

        List<String> input2 = new ArrayList<>();
        input2.add("D");
        input2.add("E");
        input2.add("F");

        List<String> input3 = new ArrayList<>();
        input3.add("B");
        input3.add("E");
        input3.add("N");

        List<String> expected = new ArrayList<>();
        expected.addAll(input1);
        expected.addAll(input2);
        expected.addAll(input3);

        List<String> actual = CollectionUtils.mergeLists(input1, input2, input3);

        assertEquals(expected, actual);
    }

}