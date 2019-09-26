package com.emarsys.mobileengage.di;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.storage.Storage;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.client.ClientServiceInternal;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.push.PushInternal;
import com.emarsys.mobileengage.push.PushTokenProvider;

public interface MobileEngageDependencyContainer extends DependencyContainer {

    MobileEngageInternal getMobileEngageInternal();

    MobileEngageInternal getLoggingMobileEngageInternal();

    ClientServiceInternal getClientServiceInternal();

    ClientServiceInternal getLoggingClientServiceInternal();

    InboxInternal getInboxInternal();

    InboxInternal getLoggingInboxInternal();

    InAppInternal getInAppInternal();

    InAppInternal getLoggingInAppInternal();

    DeepLinkInternal getDeepLinkInternal();

    DeepLinkInternal getLoggingDeepLinkInternal();

    PushInternal getPushInternal();

    PushInternal getLoggingPushInternal();

    EventServiceInternal getEventServiceInternal();

    EventServiceInternal getLoggingEventServiceInternal();

    RefreshTokenInternal getRefreshTokenInternal();

    DefaultCoreCompletionHandler getCoreCompletionHandler();

    MobileEngageRequestContext getRequestContext();

    InAppPresenter getInAppPresenter();

    NotificationEventHandler getNotificationEventHandler();

    Storage<Integer> getDeviceInfoHashStorage();

    Storage<String> getContactFieldValueStorage();

    Storage<String> getContactTokenStorage();

    Storage<String> getClientStateStorage();

    ResponseHandlersProcessor getResponseHandlersProcessor();

    NotificationCache getNotificationCache();

    PushTokenProvider getPushTokenProvider();
}
