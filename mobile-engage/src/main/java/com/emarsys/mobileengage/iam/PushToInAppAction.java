package com.emarsys.mobileengage.iam;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;

public class PushToInAppAction implements ActivityLifecycleAction {

    private final TimestampProvider timestampProvider;
    private final OverlayInAppPresenter overlayInAppPresenter;
    private final String campaignId;
    private final String html;
    private final String sid;
    private final String url;

    public PushToInAppAction(OverlayInAppPresenter overlayInAppPresenter, String campaignId, String html, String sid, String url, TimestampProvider timestampProvider) {
        Assert.notNull(overlayInAppPresenter, "InAppPresenter must not be null!");
        Assert.notNull(campaignId, "CampaignId must not be null!");
        Assert.notNull(html, "Html must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");

        this.overlayInAppPresenter = overlayInAppPresenter;
        this.campaignId = campaignId;
        this.html = html;
        this.sid = sid;
        this.url = url;
        this.timestampProvider = timestampProvider;
    }

    @Override
    public void execute(Activity activity) {
        overlayInAppPresenter.present(campaignId, sid, url, null, timestampProvider.provideTimestamp(), html, null);
    }
}
