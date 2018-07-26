package com.emarsys.mobileengage.service;

import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MobileEngageInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();

        EMSLogger.log(MobileEngageTopic.PUSH, "New token: %s", token);

        MobileEngage.setPushToken(token);
    }

}
