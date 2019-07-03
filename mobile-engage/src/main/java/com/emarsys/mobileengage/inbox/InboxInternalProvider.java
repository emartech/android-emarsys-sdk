package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.request.RequestModelFactory;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(
            RequestManager requestManager,
            RequestContext requestContext,
            RequestModelFactory requestModelFactory) {
        return new DefaultInboxInternal(requestManager, requestContext, requestModelFactory);
    }

    public InboxInternal provideLoggingInboxInternal(){
        return new LoggingInboxInternal();
    }
}
