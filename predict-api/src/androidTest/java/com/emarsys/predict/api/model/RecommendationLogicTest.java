package com.emarsys.predict.api.model;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Test(expected = IllegalArgumentException.class)
    public void testSearch_searchTerm_mustNotBeNull() {
        RecommendationLogic.search(null);
    }

    @Test
    public void testSearch_shouldFillFields_ifDataIsProvided() {
        Map<String, String> expected = new HashMap<>();
        expected.put("q", "searchTerm");

        Logic result = RecommendationLogic.search("searchTerm");
        assertEquals(expected, result.getData());
        assertEquals("SEARCH", result.getLogicName());
    }

    @Test
    public void testCart_shouldFillFields() {
        Map<String, String> expected = new HashMap<>();
        expected.put("cv", "1");
        expected.put("ca", "");

        Logic result = RecommendationLogic.cart();

        assertEquals(expected, result.getData());
        assertEquals("CART", result.getLogicName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCart_cartItems_mustNotBeNull() {
        RecommendationLogic.cart(null);
    }

    @Test
    public void testCart_shouldFillFields_ifDataIsProvided() {
        Map<String, String> expected = new HashMap<>();
        expected.put("cv", "1");
        expected.put("ca", "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0");

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new PredictCartItem("itemId1", 200.0, 100.0));
        cartItems.add(new PredictCartItem("itemId2", 201.0, 101.0));

        Logic result = RecommendationLogic.cart(cartItems);

        assertEquals(expected, result.getData());
        assertEquals("CART", result.getLogicName());
    }

    @Test
    public void testRelated_shouldFillFields() {
        Map<String, String> expected = new HashMap<>();
        expected.put("v", "");

        Logic result = RecommendationLogic.related();

        assertEquals(expected, result.getData());
        assertEquals("RELATED", result.getLogicName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelated_itemId_mustNotBeNull() {
        RecommendationLogic.related(null);
    }

    @Test
    public void testRelated_shouldFillFields_ifDataIsProvided() {
        Map<String, String> expected = new HashMap<>();
        expected.put("v", "i:itemId");

        Logic result = RecommendationLogic.related("itemId");
        assertEquals(expected, result.getData());
        assertEquals("RELATED", result.getLogicName());
    }

    @Test
    public void testCategory_shouldFillFields() {
        Map<String, String> expected = new HashMap<>();
        expected.put("vc", "");

        Logic result = RecommendationLogic.category();

        assertEquals(expected, result.getData());
        assertEquals("CATEGORY", result.getLogicName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCategory_categoryPath_mustNotBeNull() {
        RecommendationLogic.category(null);
    }

    @Test
    public void testCategory_shouldFillFields_ifDataIsProvided() {
        Map<String, String> expected = new HashMap<>();
        expected.put("vc", "testCategoryPath");

        Logic result = RecommendationLogic.category("testCategoryPath");

        assertEquals(expected, result.getData());
        assertEquals("CATEGORY", result.getLogicName());
    }

    @Test
    public void testAlsoBought_shouldFillFields() {
        Map<String, String> expected = new HashMap<>();
        expected.put("v", "");

        Logic result = RecommendationLogic.alsoBought();

        assertEquals(expected, result.getData());
        assertEquals("ALSO_BOUGHT", result.getLogicName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAlsoBought_itemId_mustNotBeNull() {
        RecommendationLogic.alsoBought(null);
    }

    @Test
    public void testAlsoBought_shouldFillFields_ifDataIsProvided() {
        Map<String, String> expected = new HashMap<>();
        expected.put("v", "i:itemId");

        Logic result = RecommendationLogic.alsoBought("itemId");

        assertEquals(expected, result.getData());
        assertEquals("ALSO_BOUGHT", result.getLogicName());
    }
}