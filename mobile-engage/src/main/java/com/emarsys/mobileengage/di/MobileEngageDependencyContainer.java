package com.emarsys.mobileengage.di;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.mobileengage.MobileEngageClientInternal;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.MobileEngageRefreshTokenInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;

public interface MobileEngageDependencyContainer extends DependencyContainer {

    MobileEngageInternal getMobileEngageInternal();

    MobileEngageRefreshTokenInternal getRefreshTokenInternal();

    MobileEngageClientInternal getClientInternal();

    InboxInternal getInboxInternal();

    InAppInternal getInAppInternal();

    DeepLinkInternal getDeepLinkInternal();

    DefaultCoreCompletionHandler getCoreCompletionHandler();

    RequestContext getRequestContext();

    InAppPresenter getInAppPresenter();

    NotificationEventHandler getNotificationEventHandler();
}
