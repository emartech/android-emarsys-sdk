package com.emarsys.mobileengage;


import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;


public class DefaultMobileEngageInternal implements MobileEngageInternal {

    private final RequestManager requestManager;
    private final MobileEngageRequestModelFactory requestModelFactory;
    private final MobileEngageRequestContext requestContext;

    public DefaultMobileEngageInternal(RequestManager requestManager,
                                       MobileEngageRequestModelFactory requestModelFactory,
                                       MobileEngageRequestContext requestContext) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.requestManager = requestManager;
        this.requestModelFactory = requestModelFactory;
        this.requestContext = requestContext;
    }

    @Override
    public void setContact(String contactFieldValue, CompletionListener completionListener) {
        requestContext.getContactFieldValueStorage().set(contactFieldValue);
        RequestModel requestModel = requestModelFactory.createSetContactRequest(contactFieldValue);
        requestManager.submit(requestModel, completionListener);
    }

    @Override
    public void clearContact(CompletionListener completionListener) {
        resetContext();
        setContact(null, completionListener);
    }

    public void resetContext() {
        requestContext.getRefreshTokenStorage().remove();
        requestContext.getContactTokenStorage().remove();
        requestContext.getContactFieldValueStorage().remove();
    }

}
