package com.emarsys.mobileengage.di;

import com.emarsys.core.di.DependencyContainer;
import com.emarsys.mobileengage.MobileEngageCoreCompletionHandler;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;

public interface MobileEngageDependencyContainer extends DependencyContainer {

    MobileEngageInternal getMobileEngageInternal();

    InboxInternal getInboxInternal();

    DeepLinkInternal getDeepLinkInternal();

    MobileEngageCoreCompletionHandler getCoreCompletionHandler();

    RequestContext getRequestContext();

    InAppPresenter getInAppPresenter();

    NotificationEventHandler getNotificationEventHandler();
}
