package com.emarsys.mobileengage.iam;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.util.Assert;

public class PushToInAppAction implements ActivityLifecycleAction {

    private InAppPresenter inAppPresenter;
    private String campaignId;
    private String html;

    public PushToInAppAction(InAppPresenter inAppPresenter, String campaignId, String html) {
        Assert.notNull(inAppPresenter, "InAppPresenter must not be null!");
        Assert.notNull(campaignId, "CampaignId must not be null!");
        Assert.notNull(html, "Html must not be null!");
        this.inAppPresenter = inAppPresenter;
        this.campaignId = campaignId;
        this.html = html;
    }

    @Override
    public void execute(Activity activity) {
        inAppPresenter.present(campaignId, html, null);
    }
}
