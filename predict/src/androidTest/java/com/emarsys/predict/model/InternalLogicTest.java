package com.emarsys.predict.model;

import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.PredictCartItem;
import com.emarsys.predict.api.model.RecommendationLogic;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalLogicTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testInternalLogic_logic_mustNotBeNull() {
        new InternalLogic(null, new LastTrackedItemContainer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInternalLogic_lastTrackedItemContainer_mustNotBeNull() {
        Logic testLogic = new TestLogic("testLogic", new HashMap<String, String>());
        new InternalLogic(testLogic, null);
    }

    @Test
    public void testGetLogicName_shouldReturnWithLogicName() {
        Logic testLogic = new TestLogic("testLogic", new HashMap<String, String>());

        InternalLogic internalLogic = new InternalLogic(testLogic, new LastTrackedItemContainer());

        Assert.assertEquals("testLogic", internalLogic.getLogicName());
    }

    @Test
    public void testGetData_shouldReturnWithLogicData() {
        Map<String, String> testData = new HashMap<>();
        testData.put("testKey", "testValue");
        Logic testLogic = new TestLogic("testLogic", testData);

        InternalLogic internalLogic = new InternalLogic(testLogic, new LastTrackedItemContainer());

        Assert.assertEquals(testData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithRelatedLogic_shouldReturnEmptyData_whenTrackingAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        Logic recommendationLogic = RecommendationLogic.related();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithRelatedLogic_shouldReturnTrackedData_whenTrackingIsNotEmptyAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("v", "i:LastViewedItemId");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastItemView("LastViewedItemId");
        Logic relatedRecommendationLogic = RecommendationLogic.related();
        InternalLogic internalLogic = new InternalLogic(relatedRecommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithRelatedLogic_shouldReturnLogicData_whenTrackingIsEmptyAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("v", "i:LastViewedItemId");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();

        Logic relatedRecommendationLogic = RecommendationLogic.related("LastViewedItemId");
        InternalLogic internalLogic = new InternalLogic(relatedRecommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithRelatedLogic_shouldReturnLogicData_whenTrackingAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("v", "i:LastViewedItemId");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastItemView("unwantedItemId");

        Logic relatedRecommendationLogic = RecommendationLogic.related("LastViewedItemId");
        InternalLogic internalLogic = new InternalLogic(relatedRecommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCartLogic_shouldReturnEmptyData_whenTrackingAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        Logic recommendationLogic = RecommendationLogic.cart();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }


    @Test
    public void testGetDataWithCartLogic_shouldReturnTrackedData_whenTrackingIsNotEmptyAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("cv", "1");
        expectedData.put("ca", "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0");

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new PredictCartItem("itemId1", 200.0, 100.0));
        cartItems.add(new PredictCartItem("itemId2", 201.0, 101.0));

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastCartItems(cartItems);

        Logic recommendationLogic = RecommendationLogic.cart();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCartLogic_shouldReturnLogicData_whenTrackingIsEmptyAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("cv", "1");
        expectedData.put("ca", "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new PredictCartItem("itemId1", 200.0, 100.0));
        cartItems.add(new PredictCartItem("itemId2", 201.0, 101.0));

        Logic recommendationLogic = RecommendationLogic.cart(cartItems);
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCartLogic_shouldReturnLogicData_whenTrackingAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("cv", "1");
        expectedData.put("ca", "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        List<CartItem> unExpectedCartItems = new ArrayList<>();
        unExpectedCartItems.add(new PredictCartItem("wrongId1", 200.0, 100.0));
        unExpectedCartItems.add(new PredictCartItem("wrongId2", 201.0, 101.0));
        lastTrackedItemContainer.setLastCartItems(unExpectedCartItems);

        List<CartItem> expectedCartItems = new ArrayList<>();
        expectedCartItems.add(new PredictCartItem("itemId1", 200.0, 100.0));
        expectedCartItems.add(new PredictCartItem("itemId2", 201.0, 101.0));

        Logic recommendationLogic = RecommendationLogic.cart(expectedCartItems);
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithSearchLogic_shouldReturnEmptyData_whenTrackingAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        Logic recommendationLogic = RecommendationLogic.search();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithSearchLogic_shouldReturnTrackedData_whenTrackingIsNotEmptyAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("q", "searchTerm");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastSearchTerm("searchTerm");
        Logic recommendationLogic = RecommendationLogic.search();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithSearchLogic_shouldReturnLogicData_whenTrackingIsEmptyAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("q", "searchTerm");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();

        Logic recommendationLogic = RecommendationLogic.search("searchTerm");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithSearchLogic_shouldReturnLogicData_whenTrackingAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("q", "searchTerm");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastSearchTerm("unwantedSearchTerm");
        Logic recommendationLogic = RecommendationLogic.search("searchTerm");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCategoryLogic_shouldReturnEmptyData_whenTrackingAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        Logic recommendationLogic = RecommendationLogic.category();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCategoryLogic_shouldReturnTrackedData_whenTrackingIsNotEmptyAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("vc", "testCategoryPath");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastCategoryPath("testCategoryPath");
        Logic recommendationLogic = RecommendationLogic.category();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCategoryLogic_shouldReturnLogicData_whenTrackingIsEmptyAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("vc", "testCategoryPath");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();

        Logic recommendationLogic = RecommendationLogic.category("testCategoryPath");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithCategoryLogic_shouldReturnLogicData_whenTrackingAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("vc", "testCategoryPath");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastCategoryPath("unwantedCategoryPath");
        Logic recommendationLogic = RecommendationLogic.category("testCategoryPath");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithAlsoBoughtLogic_shouldReturnEmptyData_whenTrackingAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        Logic recommendationLogic = RecommendationLogic.alsoBought();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithAlsoBoughtLogic_shouldReturnTrackedData_whenTrackingIsNotEmptyAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("v", "i:itemId");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastItemView("itemId");

        Logic recommendationLogic = RecommendationLogic.alsoBought();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithAlsoBoughtLogic_shouldReturnLogicData_whenTrackingIsEmptyAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("v", "i:itemId");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();

        Logic recommendationLogic = RecommendationLogic.alsoBought("itemId");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithAlsoBoughtLogic_shouldReturnLogicData_whenTrackingAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("v", "i:itemId");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastItemView("unwantedItemId");

        Logic recommendationLogic = RecommendationLogic.alsoBought("itemId");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithPopularLogic_shouldReturnEmptyData_whenTrackingAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        Logic recommendationLogic = RecommendationLogic.popular();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithPopularLogic_shouldReturnTrackedData_whenTrackingIsNotEmptyAndLogicDataIsEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("vc", "testCategoryPath");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastCategoryPath("testCategoryPath");

        Logic recommendationLogic = RecommendationLogic.popular();
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithPopularLogic_shouldReturnLogicData_whenTrackingIsEmptyAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("vc", "testCategoryPath");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();

        Logic recommendationLogic = RecommendationLogic.popular("testCategoryPath");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    @Test
    public void testGetDataWithPopularLogic_shouldReturnLogicData_whenTrackingAndLogicDataIsNotEmpty() {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("vc", "testCategoryPath");

        LastTrackedItemContainer lastTrackedItemContainer = new LastTrackedItemContainer();
        lastTrackedItemContainer.setLastCategoryPath("unwantedCategoryPath");

        Logic recommendationLogic = RecommendationLogic.popular("testCategoryPath");
        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedItemContainer);

        Assert.assertEquals(expectedData, internalLogic.getData());
    }

    static private class TestLogic implements Logic {

        private final String logic;
        private final Map<String, String> data;

        TestLogic(String logic, Map<String, String> data) {
            this.logic = logic;
            this.data = data;
        }

        @Override
        public String getLogicName() {
            return logic;
        }

        @Override
        public Map<String, String> getData() {
            return data;
        }
    }
}
