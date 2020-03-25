package com.emarsys.mobileengage.di;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.provider.activity.CurrentActivityProvider;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.storage.Storage;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.client.ClientServiceInternal;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.event.EventHandlerProvider;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.geofence.GeofenceInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.MessageInboxInternal;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.notification.ActionCommandFactory;
import com.emarsys.mobileengage.push.PushInternal;
import com.emarsys.mobileengage.push.PushTokenProvider;

public interface MobileEngageDependencyContainer extends DependencyContainer {

    MobileEngageInternal getMobileEngageInternal();

    MobileEngageInternal getLoggingMobileEngageInternal();

    ClientServiceInternal getClientServiceInternal();

    ClientServiceInternal getLoggingClientServiceInternal();

    InboxInternal getInboxInternal();

    InboxInternal getLoggingInboxInternal();

    MessageInboxInternal getMessageInboxInternal();

    MessageInboxInternal getLoggingMessageInboxInternal();

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

    Storage<Integer> getDeviceInfoHashStorage();

    Storage<String> getContactFieldValueStorage();

    Storage<String> getContactTokenStorage();

    Storage<String> getClientStateStorage();

    ResponseHandlersProcessor getResponseHandlersProcessor();

    NotificationCache getNotificationCache();

    PushTokenProvider getPushTokenProvider();

    ServiceEndpointProvider getClientServiceProvider();

    ServiceEndpointProvider getEventServiceProvider();

    ServiceEndpointProvider getDeepLinkServiceProvider();

    ServiceEndpointProvider getInboxServiceProvider();

    ServiceEndpointProvider getMessageInboxServiceProvider();

    ServiceEndpointProvider getMobileEngageV2ServiceProvider();

    Storage<String> getClientServiceStorage();

    Storage<String> getEventServiceStorage();

    Storage<String> getDeepLinkServiceStorage();

    Storage<String> getInboxServiceStorage();

    Storage<String> getMessageInboxServiceStorage();

    Storage<String> getMobileEngageV2ServiceStorage();

    ActionCommandFactory getNotificationActionCommandFactory();

    ActionCommandFactory getSilentMessageActionCommandFactory();

    EventHandlerProvider getNotificationEventHandlerProvider();

    EventHandlerProvider getSilentMessageEventHandlerProvider();

    EventHandlerProvider getGeofenceEventHandlerProvider();

    CurrentActivityProvider getCurrentActivityProvider();

    GeofenceInternal getGeofenceInternal();
}
