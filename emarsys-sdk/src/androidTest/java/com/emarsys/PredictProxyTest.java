package com.emarsys;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.PredictProxy;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.api.model.RecommendationFilter;
import com.emarsys.testUtil.RandomTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PredictProxyTest {
    private PredictInternal mockPredictInternal;
    private RunnerProxy runnerProxy;
    private PredictProxy predictProxy;
    private ResultListener<Try<List<Product>>> mockResultListener;
    private Logic mockLogic;
    private List<RecommendationFilter> mockRecommendationFilters;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        mockPredictInternal = mock(PredictInternal.class);
        runnerProxy = new RunnerProxy();

        predictProxy = new PredictProxy(runnerProxy, mockPredictInternal);
        mockResultListener = mock(ResultListener.class);

        mockLogic = mock(Logic.class);
        RecommendationFilter mockRecommendationFilter = mock(RecommendationFilter.class);
        mockRecommendationFilters = Collections.singletonList(mockRecommendationFilter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_runnerProxy_mustNotBeNull() {
        new PredictProxy(null, mockPredictInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_predictInternal_mustNotBeNull() {
        new PredictProxy(runnerProxy, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackCart_items_mustNotBeNull() {
        predictProxy.trackCart(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackCart_itemElements_mustNotBeNull() {
        predictProxy.trackCart(Arrays.asList(
                mock(CartItem.class),
                null,
                mock(CartItem.class)));
    }

    @Test
    public void testPredict_trackCart_delegatesTo_Predict_Internal() {
        List<CartItem> itemList = Arrays.asList(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0));

        predictProxy.trackCart(itemList);

        verify(mockPredictInternal).trackCart(itemList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackPurchase_orderIdMustNotBeNull() {
        predictProxy.trackPurchase(null, new ArrayList<CartItem>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackPurchase_itemsMustNotBeNull() {
        predictProxy.trackPurchase("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackPurchase_itemElements_mustNotBeNull() {
        predictProxy.trackPurchase("id", Arrays.asList(
                mock(CartItem.class),
                null,
                mock(CartItem.class)
        ));
    }

    @Test
    public void testPredict_trackPurchase_delegatesTo_Predict_Internal() {
        String orderId = "id";

        List<CartItem> itemList = Arrays.asList(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0));

        predictProxy.trackPurchase(orderId, itemList);

        verify(mockPredictInternal).trackPurchase(orderId, itemList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackItemView_itemViewId_mustNotBeNull() {
        predictProxy.trackItemView((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackItemView_product_mustNotBeNull() {
        predictProxy.trackItemView((Product) null);
    }

    @Test
    public void testPredict_trackItemView_delegatesTo_predictInternal() {
        String itemId = RandomTestUtils.randomString();

        predictProxy.trackItemView(itemId);

        verify(mockPredictInternal).trackItemView(itemId);
    }

    @Test
    public void testPredict_trackItemView_withProduct_delegatesTo_predictInternal() {
        Product product = new Product.Builder(RandomTestUtils.randomString(), RandomTestUtils.randomString(), "https://emarsys.com", RandomTestUtils.randomString()).build();

        predictProxy.trackItemView(product);

        verify(mockPredictInternal).trackItemView(product);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackCategoryView_categoryPath_mustNotBeNull() {
        predictProxy.trackCategoryView(null);
    }

    @Test
    public void testPredict_trackCategoryView_delegatesTo_predictInternal() {
        String categoryPath = RandomTestUtils.randomString();

        predictProxy.trackCategoryView(categoryPath);

        verify(mockPredictInternal).trackCategoryView(categoryPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_trackSearchTerm_searchTerm_mustNotBeNull() {
        predictProxy.trackSearchTerm(null);
    }

    @Test
    public void testPredict_trackSearchTerm_delegatesTo_predictInternal() {
        String searchTerm = RandomTestUtils.randomString();

        predictProxy.trackSearchTerm(searchTerm);

        verify(mockPredictInternal).trackSearchTerm(searchTerm);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProducts_resultListener_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProducts_recommendationLogic_mustNotBeNull() {
        predictProxy.recommendProducts(null, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProducts_limit_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, (Integer) null, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProducts_limit_mustBeAPositiveInteger() {
        predictProxy.recommendProducts(mockLogic, -5, mockResultListener);
    }

    @Test
    public void testPredict_recommendProductWithLimit_delegatesTo_predictInternal() {
        predictProxy.recommendProducts(mockLogic, 5, mockResultListener);

        verify(mockPredictInternal).recommendProducts(mockLogic, 5, null, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFilters_logic_mustNotBeNull() {
        predictProxy.recommendProducts(null, mockRecommendationFilters, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFilters_resultListener_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, mockRecommendationFilters, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFilters_recommendationFilter_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, (List<RecommendationFilter>) null, mockResultListener);
    }

    @Test
    public void testPredict_recommendProductsWithFilters_delegatesTo_predictInternal() {
        predictProxy.recommendProducts(mockLogic, mockRecommendationFilters, mockResultListener);

        verify(mockPredictInternal).recommendProducts(mockLogic, null, mockRecommendationFilters, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFiltersAndLimit_logic_mustNotBeNull() {
        predictProxy.recommendProducts(null, 5, mockRecommendationFilters, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFiltersAndLimit_recommendationFilter_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, 5, null, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFiltersAndLimit_limit_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, null, mockRecommendationFilters, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFiltersAndLimit_limit_mustBeAPositiveInteger() {
        predictProxy.recommendProducts(mockLogic, -5, mockRecommendationFilters, mockResultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_recommendProductsWithFiltersAndLimit_resultListener_mustNotBeNull() {
        predictProxy.recommendProducts(mockLogic, 5, mockRecommendationFilters, null);
    }

    @Test
    public void testPredict_recommendProductsWithFiltersAndLimit_delegatesTo_predictInternal() {
        predictProxy.recommendProducts(mockLogic, 123, mockRecommendationFilters, mockResultListener);

        verify(mockPredictInternal).recommendProducts(mockLogic, 123, mockRecommendationFilters, mockResultListener);
    }

    @Test
    public void testPredict_recommendProducts_delegatesTo_predictInternal() {
        predictProxy.recommendProducts(mockLogic, mockResultListener);

        verify(mockPredictInternal).recommendProducts(mockLogic, null, null, mockResultListener);
    }

    private CartItem createItem(final String id, final double price, final double quantity) {
        return new CartItem() {
            @Override
            public String getItemId() {
                return id;
            }

            @Override
            public double getPrice() {
                return price;
            }

            @Override
            public double getQuantity() {
                return quantity;
            }
        };
    }

}