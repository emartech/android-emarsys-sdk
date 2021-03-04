package com.emarsys.mobileengage.iam.dialog.action;

import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.EventServiceInternal;

import java.util.HashMap;
import java.util.Map;

public class SendDisplayedIamAction implements OnDialogShownAction {

    private final CoreSdkHandler handler;
    private final EventServiceInternal eventServiceInternal;

    public SendDisplayedIamAction(
            CoreSdkHandler handler,
            EventServiceInternal eventServiceInternal) {
        Assert.notNull(handler, "Handler must not be null!");
        Assert.notNull(eventServiceInternal, "EventServiceInternal must not be null!");
        this.handler = handler;
        this.eventServiceInternal = eventServiceInternal;
    }

    @Override
    public void execute(final String campaignId, final String sid, final String url) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("campaignId", campaignId);
                if (sid != null) {
                    attributes.put("sid", sid);
                }
                if (url != null) {
                    attributes.put("url", url);
                }

                String eventName = "inapp:viewed";
                eventServiceInternal.trackInternalCustomEventAsync(eventName, attributes, null);
            }
        });
    }
}
