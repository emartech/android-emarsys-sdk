package com.emarsys.mobileengage.iam.jsbridge;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.api.EventHandler;

public class InAppMessageHandlerProvider {

    public EventHandler provideHandler() {
        return MobileEngage.getConfig().getDefaultInAppEventHandler();
    }

}
