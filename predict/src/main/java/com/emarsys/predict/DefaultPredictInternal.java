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
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelFactory;
import com.emarsys.predict.util.CartItemUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public DefaultPredictInternal(PredictRequestContext requestContext, RequestManager requestManager, PredictRequestModelFactory requestModelFactory) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");

        this.keyValueStore = requestContext.getKeyValueStore();
        this.requestManager = requestManager;
        this.uuidProvider = requestContext.getUuidProvider();
        this.timestampProvider = requestContext.getTimestampProvider();
        this.requestModelFactory = requestModelFactory;
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
        return shard.getId();
    }

    @Override
    public void recommendProducts(final ResultListener<Try<List<Product>>> resultListener) {
        Assert.notNull(resultListener, "ResultListener must not be null!");

        RequestModel requestModel = requestModelFactory.createRecommendationRequest();

        requestManager.submitNow(requestModel, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {

                List<Product> products = parseResponse(responseModel);

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

    private List<Product> parseResponse(ResponseModel responseModel) {
        List<Product> result = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(responseModel.getBody());
            JSONObject products = json.getJSONObject("products");
            for (int i = 0; i < products.names().length(); i++) {
                Map<String, String> customFields = new HashMap<>();

                JSONObject product = products.getJSONObject(products.names().getString(i));

                customFields.put("msrp_gpb", product.getString("msrp_gpb"));
                customFields.put("price_gpb", product.getString("price_gpb"));
                customFields.put("msrp_aed", product.getString("msrp_aed"));
                customFields.put("price_aed", product.getString("price_aed"));
                customFields.put("msrp_cad", product.getString("msrp_cad"));
                customFields.put("price_cad", product.getString("price_cad"));
                customFields.put("msrp_mxn", product.getString("msrp_mxn"));
                customFields.put("price_mxn", product.getString("price_mxn"));
                customFields.put("msrp_pln", product.getString("msrp_pln"));
                customFields.put("price_pln", product.getString("price_pln"));
                customFields.put("msrp_rub", product.getString("msrp_rub"));
                customFields.put("price_rub", product.getString("price_rub"));
                customFields.put("msrp_sek", product.getString("msrp_sek"));
                customFields.put("price_sek", product.getString("price_sek"));
                customFields.put("msrp_try", product.getString("msrp_try"));
                customFields.put("price_try", product.getString("price_try"));
                customFields.put("msrp_usd", product.getString("msrp_usd"));
                customFields.put("price_usd", product.getString("price_usd"));

                result.add(new Product.Builder(
                        product.getString("item"),
                        product.getString("title"),
                        product.getString("link")
                ).categoryPath(product.getString("category"))
                        .available(product.getBoolean("available"))
                        .msrp(Float.parseFloat(product.getString("msrp")))
                        .price(Float.parseFloat(product.getString("price")))
                        .imageUrl(product.getString("image"))
                        .zoomImageUrl(product.getString("zoom_image"))
                        .customFields(customFields)
                        .build());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
