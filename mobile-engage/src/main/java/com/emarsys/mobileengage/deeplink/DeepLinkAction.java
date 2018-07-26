package com.emarsys.mobileengage.deeplink;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.util.Assert;

public class DeepLinkAction implements ActivityLifecycleAction {

    private DeepLinkInternal deepLinkInternal;

    public DeepLinkAction(DeepLinkInternal deepLinkInternal) {
        Assert.notNull(deepLinkInternal, "DeepLinkInternal must not be null!");
        this.deepLinkInternal = deepLinkInternal;
    }

    @Override
    public void execute(Activity activity) {
        if (activity != null && activity.getIntent() != null) {
            deepLinkInternal.trackDeepLinkOpen(activity, activity.getIntent());
        }
    }

}
