package com.emarsys.predict.api.model;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RecommendationLogicTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    public void testSearch_shouldFillFields() {
        Map<String, String> expected = new HashMap<>();
        expected.put("q", "");

        Logic result = RecommendationLogic.search();

        assertEquals(expected, result.getData());
        assertEquals("SEARCH", result.getLogicName());
    }

    @Test
    public void testSearch_shouldFillFields_ifDataIsProvided() {
        Map<String, String> expected = new HashMap<>();
        expected.put("q", "searchTerm");

        Logic result = RecommendationLogic.search("searchTerm");
        assertEquals(expected, result.getData());
        assertEquals("SEARCH", result.getLogicName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearch_searchTerm_mustNotBeNull() {
        RecommendationLogic.search(null);
    }

}