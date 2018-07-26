package com.emarsys.mobileengage.iam;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;

public class InAppStartAction implements ActivityLifecycleAction {

    private MobileEngageInternal mobileEngageInternal;

    public InAppStartAction(MobileEngageInternal mobileEngageInternal) {
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.mobileEngageInternal = mobileEngageInternal;
    }

    @Override
    public void execute(Activity activity) {
        mobileEngageInternal.trackInternalCustomEvent("app:start", null);
    }
}
