package com.emarsys.di;

import com.emarsys.inapp.InAppApi;
import com.emarsys.inbox.InboxApi;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.predict.PredictApi;
import com.emarsys.predict.di.PredictDependencyContainer;
import com.emarsys.push.PushApi;

public interface EmarysDependencyContainer extends MobileEngageDependencyContainer, PredictDependencyContainer {
    InboxApi getInbox();

    InAppApi getInApp();

    PushApi getPush();

    PredictApi getPredict();
}
