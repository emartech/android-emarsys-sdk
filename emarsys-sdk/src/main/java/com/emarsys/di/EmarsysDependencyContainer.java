package com.emarsys.di;

import com.emarsys.config.ConfigApi;
import com.emarsys.config.ConfigInternal;
import com.emarsys.inapp.InAppApi;
import com.emarsys.inbox.InboxApi;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.predict.PredictApi;
import com.emarsys.predict.di.PredictDependencyContainer;
import com.emarsys.push.PushApi;

public interface EmarsysDependencyContainer extends MobileEngageDependencyContainer, PredictDependencyContainer {
    InboxApi getInbox();

    InboxApi getLoggingInbox();

    InAppApi getInApp();

    InAppApi getLoggingInApp();

    PushApi getPush();

    PushApi getLoggingPush();

    PredictApi getPredict();

    PredictApi getLoggingPredict();

    ConfigApi getConfig();

    ConfigInternal getConfigInternal();
}
