package com.emarsys.predict;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.model.InternalLogic;
import com.emarsys.predict.model.LastTrackedItemContainer;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelFactory;
import com.emarsys.predict.util.CartItemUtils;

import java.util.List;

public class DefaultPredictInternal implements PredictInternal {

    public static final String VISITOR_ID_KEY = "predict_visitor_id";
    public static final String CONTACT_ID_KEY = "predict_contact_id";

    private static final String TYPE_CART = "predict_cart";
    private static final String TYPE_PURCHASE = "predict_purchase";
    private static final String TYPE_ITEM_VIEW = "predict_item_view";
    private static final String TYPE_CATEGORY_VIEW = "predict_category_view";
    private static final String TYPE_SEARCH_TERM = "predict_search_term";

    private final UUIDProvider uuidProvider;
    private final TimestampProvider timestampProvider;
    private final KeyValueStore keyValueStore;
    private final RequestManager requestManager;
    private final PredictRequestModelFactory requestModelFactory;
    private final PredictResponseMapper responseMapper;
    private final LastTrackedItemContainer lastTrackedContainer;

    public DefaultPredictInternal(PredictRequestContext requestContext, RequestManager requestManager, PredictRequestModelFactory requestModelFactory, PredictResponseMapper responseMapper) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");
        Assert.notNull(responseMapper, "ResponseMapper must not be null!");

        this.keyValueStore = requestContext.getKeyValueStore();
        this.requestManager = requestManager;
        this.uuidProvider = requestContext.getUuidProvider();
        this.timestampProvider = requestContext.getTimestampProvider();
        this.requestModelFactory = requestModelFactory;
        this.responseMapper = responseMapper;
        lastTrackedContainer = new LastTrackedItemContainer();
    }

    @Override
    public void setContact(String contactId) {
        Assert.notNull(contactId, "ContactId must not be null!");
        keyValueStore.putString(CONTACT_ID_KEY, contactId);
    }

    @Override
    public void clearContact() {
        keyValueStore.remove(CONTACT_ID_KEY);
        keyValueStore.remove(VISITOR_ID_KEY);
    }

    @Override
    public String trackCart(List<CartItem> items) {
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_CART)
                .payloadEntry("cv", 1)
                .payloadEntry("ca", CartItemUtils.cartItemsToQueryParam(items))
                .build();

        requestManager.submit(shard);

        lastTrackedContainer.setLastCartItems(items);
        return shard.getId();
    }

    @Override
    public String trackPurchase(String orderId, List<CartItem> items) {
        Assert.notNull(orderId, "OrderId must not be null!");
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_PURCHASE)
                .payloadEntry("oi", orderId)
                .payloadEntry("co", CartItemUtils.cartItemsToQueryParam(items))
                .build();

        requestManager.submit(shard);
        lastTrackedContainer.setLastCartItems(items);
        return shard.getId();
    }

    @Override
    public String trackItemView(String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_ITEM_VIEW)
                .payloadEntry("v", "i:" + itemId)
                .build();

        requestManager.submit(shard);
        lastTrackedContainer.setLastItemView(itemId);
        return shard.getId();
    }

    @Override
    public String trackCategoryView(String categoryPath) {
        Assert.notNull(categoryPath, "CategoryPath must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_CATEGORY_VIEW)
                .payloadEntry("vc", categoryPath)
                .build();

        requestManager.submit(shard);
        lastTrackedContainer.setLastCategoryPath(categoryPath);
        return shard.getId();
    }

    @Override
    public String trackSearchTerm(String searchTerm) {
        Assert.notNull(searchTerm, "SearchTerm must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_SEARCH_TERM)
                .payloadEntry("q", searchTerm)
                .build();

        requestManager.submit(shard);
        lastTrackedContainer.setLastSearchTerm(searchTerm);
        return shard.getId();
    }

    @Override
    public void recommendProducts(Logic recommendationLogic, final ResultListener<Try<List<Product>>> resultListener) {
        Assert.notNull(recommendationLogic, "RecommendationLogic must not be null!");
        Assert.notNull(resultListener, "ResultListener must not be null!");

        InternalLogic internalLogic = new InternalLogic(recommendationLogic, lastTrackedContainer);
        RequestModel requestModel = requestModelFactory.createRecommendationRequest(internalLogic);

        requestManager.submitNow(requestModel, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {

                List<Product> products = responseMapper.map(responseModel);

                resultListener.onResult(Try.success(products));
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                resultListener.onResult(Try.failure(new ResponseErrorException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody())));
            }

            @Override
            public void onError(String id, Exception cause) {
                resultListener.onResult(Try.failure(cause));
            }
        });
    }
}
