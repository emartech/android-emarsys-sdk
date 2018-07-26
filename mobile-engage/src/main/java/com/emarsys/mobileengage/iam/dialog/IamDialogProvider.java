package com.emarsys.mobileengage.iam.dialog;

import android.os.Build;
import android.support.annotation.RequiresApi;

public class IamDialogProvider {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public IamDialog provideDialog(String campaignId) {
        return IamDialog.create(campaignId);
    }

}
