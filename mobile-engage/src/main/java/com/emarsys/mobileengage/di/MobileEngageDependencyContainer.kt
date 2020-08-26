package com.emarsys.mobileengage.di

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.WebViewProvider
import com.emarsys.mobileengage.inbox.InboxInternal
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler

interface MobileEngageDependencyContainer : DependencyContainer {
    fun getMobileEngageInternal(): MobileEngageInternal

    fun getLoggingMobileEngageInternal(): MobileEngageInternal

    fun getClientServiceInternal(): ClientServiceInternal

    fun getLoggingClientServiceInternal(): ClientServiceInternal

    fun getInboxInternal(): InboxInternal

    fun getLoggingInboxInternal(): InboxInternal

    fun getMessageInboxInternal(): MessageInboxInternal

    fun getLoggingMessageInboxInternal(): MessageInboxInternal

    fun getInAppInternal(): InAppInternal

    fun getLoggingInAppInternal(): InAppInternal

    fun getDeepLinkInternal(): DeepLinkInternal

    fun getLoggingDeepLinkInternal(): DeepLinkInternal

    fun getPushInternal(): PushInternal

    fun getLoggingPushInternal(): PushInternal

    fun getEventServiceInternal(): EventServiceInternal

    fun getLoggingEventServiceInternal(): EventServiceInternal

    fun getRefreshTokenInternal(): RefreshTokenInternal

    fun getCoreCompletionHandler(): CoreCompletionHandler

    fun getRequestContext(): MobileEngageRequestContext

    fun getOverlayInAppPresenter(): OverlayInAppPresenter

    fun getDeviceInfoPayloadStorage(): Storage<String?>

    fun getContactFieldValueStorage(): Storage<String?>

    fun getContactTokenStorage(): Storage<String?>

    fun getClientStateStorage(): Storage<String?>

    fun getPushTokenStorage(): Storage<String?>

    fun getRefreshContactTokenStorage(): Storage<String?>

    fun getLogLevelStorage(): Storage<String?>

    fun getResponseHandlersProcessor(): ResponseHandlersProcessor

    fun getNotificationCache(): NotificationCache

    fun getPushTokenProvider(): PushTokenProvider

    fun getClientServiceProvider(): ServiceEndpointProvider

    fun getEventServiceProvider(): ServiceEndpointProvider

    fun getDeepLinkServiceProvider(): ServiceEndpointProvider

    fun getInboxServiceProvider(): ServiceEndpointProvider

    fun getMessageInboxServiceProvider(): ServiceEndpointProvider

    fun getMobileEngageV2ServiceProvider(): ServiceEndpointProvider

    fun getNotificationInformationListenerProvider(): NotificationInformationListenerProvider

    fun getSilentNotificationInformationListenerProvider(): SilentNotificationInformationListenerProvider

    fun getClientServiceStorage(): Storage<String?>

    fun getEventServiceStorage(): Storage<String?>

    fun getDeepLinkServiceStorage(): Storage<String?>

    fun getInboxServiceStorage(): Storage<String?>

    fun getMessageInboxServiceStorage(): Storage<String?>

    fun getMobileEngageV2ServiceStorage(): Storage<String?>

    fun getNotificationActionCommandFactory(): ActionCommandFactory

    fun getSilentMessageActionCommandFactory(): ActionCommandFactory

    fun getNotificationEventHandlerProvider(): EventHandlerProvider

    fun getSilentMessageEventHandlerProvider(): EventHandlerProvider

    fun getGeofenceEventHandlerProvider(): EventHandlerProvider

    fun getCurrentActivityProvider(): CurrentActivityProvider

    fun getGeofenceInternal(): GeofenceInternal

    fun getLoggingGeofenceInternal(): GeofenceInternal

    fun getButtonClickedRepository(): Repository<ButtonClicked, SqlSpecification>

    fun getDisplayedIamRepository(): Repository<DisplayedIam, SqlSpecification>

    fun getContactTokenResponseHandler(): MobileEngageTokenResponseHandler

    fun getWebViewProvider(): WebViewProvider
}