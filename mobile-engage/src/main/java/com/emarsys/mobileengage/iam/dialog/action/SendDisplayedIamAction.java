package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;

import java.util.HashMap;
import java.util.Map;

public class SendDisplayedIamAction implements OnDialogShownAction {

    private Handler handler;
    private MobileEngageInternal mobileEngageInternal;

    public SendDisplayedIamAction(
            Handler handler,
            MobileEngageInternal mobileEngageInternal) {
        Assert.notNull(handler, "Handler must not be null!");
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.handler = handler;
        this.mobileEngageInternal = mobileEngageInternal;
    }

    @Override
    public void execute(final String campaignId) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("message_id", campaignId);

                String eventName = "inapp:viewed";
                mobileEngageInternal.trackInternalCustomEvent(eventName, attributes);
            }
        });
    }
}
