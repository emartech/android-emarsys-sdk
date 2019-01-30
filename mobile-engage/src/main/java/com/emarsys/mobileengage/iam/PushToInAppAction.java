package com.emarsys.mobileengage.iam;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;

public class PushToInAppAction implements ActivityLifecycleAction {

    private final TimestampProvider timestampProvider;
    private final InAppPresenter inAppPresenter;
    private final String campaignId;
    private final String html;

    public PushToInAppAction(InAppPresenter inAppPresenter, String campaignId, String html, TimestampProvider timestampProvider) {
        Assert.notNull(inAppPresenter, "InAppPresenter must not be null!");
        Assert.notNull(campaignId, "CampaignId must not be null!");
        Assert.notNull(html, "Html must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");

        this.inAppPresenter = inAppPresenter;
        this.campaignId = campaignId;
        this.html = html;
        this.timestampProvider = timestampProvider;
    }

    @Override
    public void execute(Activity activity) {
        inAppPresenter.present(campaignId, null, timestampProvider.provideTimestamp(), html, null);
    }
}
