package com.emarsys.mobileengage.iam;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.EventServiceInternal;

public class InAppStartAction implements ActivityLifecycleAction {

    private EventServiceInternal eventServiceInternal;
    private Storage<String> contactTokenStorage;

    public InAppStartAction(EventServiceInternal eventServiceInternal, Storage<String> contactTokenStorage) {
        Assert.notNull(eventServiceInternal, "EventServiceInternal must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");

        this.eventServiceInternal = eventServiceInternal;
        this.contactTokenStorage = contactTokenStorage;
    }

    @Override
    public void execute(Activity activity) {
        if (contactTokenStorage.get() != null) {
            eventServiceInternal.trackInternalCustomEvent("app:start", null, null);
        }
    }
}
