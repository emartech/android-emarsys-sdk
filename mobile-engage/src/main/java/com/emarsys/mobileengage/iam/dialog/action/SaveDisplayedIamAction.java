package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

public class SaveDisplayedIamAction implements OnDialogShownAction {

    Handler handler;
    Repository<DisplayedIam, SqlSpecification> repository;
    TimestampProvider timestampProvider;

    public SaveDisplayedIamAction(Handler handler, Repository<DisplayedIam, SqlSpecification> repository, TimestampProvider timestampProvider) {
        Assert.notNull(handler, "Handler must not be null!");
        Assert.notNull(repository, "Repository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.handler = handler;
        this.repository = repository;
        this.timestampProvider = timestampProvider;
    }

    @Override
    public void execute(final String campaignId) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        handler.post(new Runnable() {
            @Override
            public void run() {
                DisplayedIam iam = new DisplayedIam(campaignId, timestampProvider.provideTimestamp());
                repository.add(iam);
            }
        });
    }
}
