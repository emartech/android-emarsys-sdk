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

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logicName_mustNotBeNull() {
        new RecommendationLogic(null, new LogicData(new HashMap<String, String>()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logicData_mustNotBeNull() {
        new RecommendationLogic(RecommendationLogic.SEARCH, null);
    }

    @Test
    public void testSearch_shouldFillFields() {
        LogicData expected = new LogicData(new HashMap<String, String>());

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
        Map<String, String> data = new HashMap<>();
        data.put("q", "searchTerm");
        LogicData expected = new LogicData(data);

        Logic result = RecommendationLogic.search("searchTerm");
        assertEquals(expected, result.getData());
        assertEquals("SEARCH", result.getLogicName());
    }

    @Test
    public void testCart_shouldFillFields() {
        LogicData expected = new LogicData(new HashMap<String, String>());

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
        Map<String, String> data = new HashMap<>();
        data.put("cv", "1");
        data.put("ca", "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0");
        LogicData expected = new LogicData(data);


        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new PredictCartItem("itemId1", 200.0, 100.0));
        cartItems.add(new PredictCartItem("itemId2", 201.0, 101.0));

        Logic result = RecommendationLogic.cart(cartItems);

        assertEquals(expected, result.getData());
        assertEquals("CART", result.getLogicName());
    }

    @Test
    public void testRelated_shouldFillFields() {
       LogicData expected = new LogicData(new HashMap<String, String>());

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
        Map<String, String> data = new HashMap<>();
        data.put("v", "i:itemId");
        LogicData expected = new LogicData(data);


        Logic result = RecommendationLogic.related("itemId");
        assertEquals(expected, result.getData());
        assertEquals("RELATED", result.getLogicName());
    }

    @Test
    public void testCategory_shouldFillFields() {
        LogicData expected = new LogicData(new HashMap<String, String>());

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
        Map<String, String> data = new HashMap<>();
        data.put("vc", "testCategoryPath");
        LogicData expected = new LogicData(data);

        Logic result = RecommendationLogic.category("testCategoryPath");

        assertEquals(expected, result.getData());
        assertEquals("CATEGORY", result.getLogicName());
    }

    @Test
    public void testAlsoBought_shouldFillFields() {
        LogicData expected = new LogicData(new HashMap<String, String>());

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
        Map<String, String> data = new HashMap<>();
        data.put("v", "i:itemId");
        LogicData expected = new LogicData(data);

        Logic result = RecommendationLogic.alsoBought("itemId");

        assertEquals(expected, result.getData());
        assertEquals("ALSO_BOUGHT", result.getLogicName());
    }

    @Test
    public void testPopular_shouldFillFields() {
        LogicData expected = new LogicData(new HashMap<String, String>());

        Logic result = RecommendationLogic.popular();

        assertEquals(expected, result.getData());
        assertEquals("POPULAR", result.getLogicName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPopular_categoryPath_mustNotBeNull() {
        RecommendationLogic.popular(null);
    }

    @Test
    public void testPopular_shouldFillFields_ifDataIsProvided() {
        Map<String, String> data = new HashMap<>();
        data.put("vc", "testCategoryPath");
        LogicData expected = new LogicData(data);


        Logic result = RecommendationLogic.popular("testCategoryPath");

        assertEquals(expected, result.getData());
        assertEquals("POPULAR", result.getLogicName());
    }


    @Test
    public void testPersonal_shouldFillFields() {
        LogicData expected = new LogicData(new HashMap<String, String>());

        Logic result = RecommendationLogic.personal();

        assertEquals(expected, result.getData());
        assertEquals("PERSONAL", result.getLogicName());
    }


    @Test
    public void testPersonal_shouldFillFields_withExtensions() {
        List<String> extensions = new ArrayList<>();
        extensions.add("1");
        extensions.add("2");
        extensions.add("3");
        LogicData expected = new LogicData(new HashMap<String, String>(), extensions);

        Logic result = RecommendationLogic.personal(extensions);

        assertEquals(expected, result.getData());
    }
}