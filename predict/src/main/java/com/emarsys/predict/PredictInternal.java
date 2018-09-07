package com.emarsys.predict;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.CartItem;

import java.util.List;

public class PredictInternal {

    public static final String COMMAND_CART = "cart";
    private static final String COMMAND_VIEW = "view";

    public static final String TYPE_CART = "predict_cart";
    private static final String TYPE_ITEM_VIEW = "predict_item_view";

    private final UUIDProvider uuidProvider;
    private final TimestampProvider timestampProvider;
    private final KeyValueStore keyValueStore;
    private final RequestManager requestManager;

    public PredictInternal(KeyValueStore keyValueStore, RequestManager requestManager, UUIDProvider uuidProvider, TimestampProvider timestampProvider) {
        Assert.notNull(keyValueStore, "KeyValueStore must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.keyValueStore = keyValueStore;
        this.requestManager = requestManager;
        this.uuidProvider = uuidProvider;
        this.timestampProvider = timestampProvider;
    }

    public void setCustomer(String customerId) {
        Assert.notNull(customerId, "CustomerId must not be null!");
        keyValueStore.putString("predict_customerId", customerId);
    }

    public void trackCart(List<CartItem> items) {
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        ShardModel itemViewShard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .payloadEntry(COMMAND_CART, items)
                .type(TYPE_CART)
                .build();

        requestManager.submit(itemViewShard);
    }

    public void trackPurchase(String orderId, List<CartItem> items) {
    }

    public void trackItemView(String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");

        ShardModel itemViewShard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .payloadEntry(COMMAND_VIEW, itemId)
                .type(TYPE_ITEM_VIEW)
                .build();

        requestManager.submit(itemViewShard);
    }

    public void trackCategoryView(String categoryPath) {
    }

    public void trackSearchTerm(String term) {
    }

}
