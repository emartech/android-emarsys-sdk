package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.InAppInternal;

import java.util.HashMap;
import java.util.Map;

public class SendDisplayedIamAction implements OnDialogShownAction {

    private Handler handler;
    private InAppInternal inAppInternal;

    public SendDisplayedIamAction(
            Handler handler,
            InAppInternal inAppInternal) {
        Assert.notNull(handler, "Handler must not be null!");
        Assert.notNull(inAppInternal, "InAppInternal must not be null!");
        this.handler = handler;
        this.inAppInternal = inAppInternal;
    }

    @Override
    public void execute(final String campaignId) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("campaignId", campaignId);

                String eventName = "inapp:viewed";
                inAppInternal.trackInternalCustomEvent(eventName, attributes, null);
            }
        });
    }
}
