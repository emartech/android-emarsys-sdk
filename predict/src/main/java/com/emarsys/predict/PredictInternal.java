package com.emarsys.predict;

import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;

public class PredictInternal {

    private KeyValueStore keyValueStore;

    public PredictInternal(KeyValueStore keyValueStore) {
        Assert.notNull(keyValueStore, "KeyValueStore must not be null!");

        this.keyValueStore = keyValueStore;
    }

    public void setCustomer(String customerId) {
        Assert.notNull(customerId, "CustomerId must not be null!");
        keyValueStore.putString("predict_customerId", customerId);
    }
}
