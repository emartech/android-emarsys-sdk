package com.emarsys.mobileengage.di

import android.content.ClipboardManager
import com.emarsys.core.app.AppLifecycleObserver
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.CoreComponent
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamWebViewFactory
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.mobileengage.request.CoreCompletionHandlerRefreshTokenProxyProvider
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperFactory
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.mobileengage.util.RequestModelHelper
import com.google.android.gms.location.FusedLocationProviderClient

fun mobileEngage() = MobileEngageComponent.instance
    ?: throw IllegalStateException("DependencyContainer has to be setup first!")

fun setupMobileEngageComponent(mobileEngageComponent: MobileEngageComponent) {
    MobileEngageComponent.instance = mobileEngageComponent
    CoreComponent.instance = mobileEngageComponent
}

fun tearDownMobileEngageComponent() {
    MobileEngageComponent.instance = null
    CoreComponent.instance = null
}

fun isMobileEngageComponentSetup() =
    MobileEngageComponent.instance != null &&
            CoreComponent.instance != null

interface MobileEngageComponent : CoreComponent {
    companion object {
        var instance: MobileEngageComponent? = null
    }

    val notificationOpenedActivityClass: Class<*>

    val mobileEngageInternal: MobileEngageInternal

    val loggingMobileEngageInternal: MobileEngageInternal

    val clientServiceInternal: ClientServiceInternal

    val loggingClientServiceInternal: ClientServiceInternal

    val messageInboxInternal: MessageInboxInternal

    val loggingMessageInboxInternal: MessageInboxInternal

    val inAppInternal: InAppInternal

    val loggingInAppInternal: InAppInternal

    val deepLinkInternal: DeepLinkInternal

    val pushInternal: PushInternal

    val loggingPushInternal: PushInternal

    val eventServiceInternal: EventServiceInternal

    val loggingEventServiceInternal: EventServiceInternal

    val inAppEventHandlerInternal: InAppEventHandlerInternal

    val requestContext: MobileEngageRequestContext

    val overlayInAppPresenter: OverlayInAppPresenter

    val clipboardManager: ClipboardManager

    val deviceInfoPayloadStorage: Storage<String?>

    val contactFieldValueStorage: Storage<String?>

    val contactTokenStorage: Storage<String?>

    val clientStateStorage: Storage<String?>

    val pushTokenStorage: Storage<String?>

    val localPushTokenStorage: Storage<String?>

    val refreshTokenStorage: Storage<String?>

    val clientServiceStorage: Storage<String?>

    val eventServiceStorage: Storage<String?>

    val deepLinkServiceStorage: Storage<String?>

    val messageInboxServiceStorage: Storage<String?>

    val deviceEventStateStorage: Storage<String?>

    val geofenceInitialEnterTriggerEnabledStorage: Storage<Boolean?>

    val fusedLocationProviderClient: FusedLocationProviderClient

    val responseHandlersProcessor: ResponseHandlersProcessor

    val pushTokenProvider: PushTokenProvider

    val clientServiceEndpointProvider: ServiceEndpointProvider

    val eventServiceEndpointProvider: ServiceEndpointProvider

    val deepLinkServiceProvider: ServiceEndpointProvider

    val messageInboxServiceProvider: ServiceEndpointProvider

    val notificationInformationListenerProvider: NotificationInformationListenerProvider

    val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider

    val notificationActionCommandFactory: ActionCommandFactory

    val silentMessageActionCommandFactory: ActionCommandFactory

    val notificationCacheableEventHandler: EventHandler

    val silentMessageCacheableEventHandler: EventHandler

    val onEventActionCacheableEventHandler: CacheableEventHandler

    val geofenceCacheableEventHandler: EventHandler

    val currentActivityProvider: CurrentActivityProvider

    val geofenceInternal: GeofenceInternal

    val loggingGeofenceInternal: GeofenceInternal

    val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>

    val displayedIamRepository: Repository<DisplayedIam, SqlSpecification>

    val contactTokenResponseHandler: MobileEngageTokenResponseHandler

    val webViewFactory: IamWebViewFactory

    val iamJsBridgeFactory: IamJsBridgeFactory

    val jsCommandFactoryProvider: JSCommandFactoryProvider

    val jsOnCloseListener: OnCloseListener

    val jsOnAppEventListener: OnAppEventListener

    val remoteMessageMapperFactory: RemoteMessageMapperFactory

    val appLifecycleObserver: AppLifecycleObserver

    val requestModelHelper: RequestModelHelper

    val sessionIdHolder: SessionIdHolder

    val coreCompletionHandlerRefreshTokenProxyProvider: CoreCompletionHandlerRefreshTokenProxyProvider

    val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory

    val mobileEngageSession: MobileEngageSession
}