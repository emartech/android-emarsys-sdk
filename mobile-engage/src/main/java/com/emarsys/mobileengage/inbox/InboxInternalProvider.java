package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.request.RequestModelFactory;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(boolean experimental,
                                              RequestManager requestManager,
                                              RequestContext requestContext,
                                              RequestModelFactory requestModelFactory) {
        InboxInternal result;
        if (experimental) {
            result = new InboxInternal_V2(requestManager, requestContext, requestModelFactory);
        } else {
            result = new InboxInternal_V1(requestManager, requestContext);
        }
        return result;
    }

}
