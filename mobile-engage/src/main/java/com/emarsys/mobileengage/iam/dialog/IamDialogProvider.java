package com.emarsys.mobileengage.iam.dialog;

public class IamDialogProvider {

    public IamDialog provideDialog(String campaignId, String sid, String url, String requestId) {
        return IamDialog.create(campaignId, sid, url, requestId);
    }

}
