package com.emarsys.di

import com.emarsys.clientservice.ClientServiceApi
import com.emarsys.config.ConfigApi
import com.emarsys.config.ConfigInternal
import com.emarsys.core.di.CoreComponent
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageApi
import com.emarsys.mobileengage.di.MobileEngageComponent
import com.emarsys.oneventaction.OnEventActionApi
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictRestrictedApi
import com.emarsys.predict.di.PredictComponent
import com.emarsys.push.PushApi
import com.emarsys.request.CoreCompletionHandlerRefreshTokenProxyProvider

fun emarsys() = EmarsysComponent.instance
        ?: throw IllegalStateException("DependencyContainer has to be setup first!")

fun setupEmarsysComponent(emarsysComponent: EmarsysComponent) {
    EmarsysComponent.instance = emarsysComponent
    MobileEngageComponent.instance = emarsysComponent
    PredictComponent.instance = emarsysComponent
    CoreComponent.instance = emarsysComponent
}

fun tearDownEmarsysComponent() {
    EmarsysComponent.instance = null
    MobileEngageComponent.instance = null
    PredictComponent.instance = null
    CoreComponent.instance = null
}

fun isEmarsysComponentSetup() =
        EmarsysComponent.instance != null &&
                MobileEngageComponent.instance != null &&
                PredictComponent.instance != null &&
                CoreComponent.instance != null

interface EmarsysComponent : MobileEngageComponent, PredictComponent {
    companion object {
        var instance: EmarsysComponent? = null
    }

    val messageInbox: MessageInboxApi

    val loggingMessageInbox: MessageInboxApi

    val deepLink: DeepLinkApi
    
    val inApp: InAppApi

    val loggingInApp: InAppApi

    val onEventAction: OnEventActionApi

    val loggingOnEventAction: OnEventActionApi

    val push: PushApi

    val loggingPush: PushApi

    val predict: PredictApi

    val loggingPredict: PredictApi

    val predictRestricted: PredictRestrictedApi

    val loggingPredictRestricted: PredictRestrictedApi

    val config: ConfigApi

    val geofence: GeofenceApi

    val loggingGeofence: GeofenceApi

    val mobileEngage: MobileEngageApi

    val loggingMobileEngage: MobileEngageApi

    val configInternal: ConfigInternal

    val clientService: ClientServiceApi

    val loggingClientService: ClientServiceApi

    val eventService: EventServiceApi

    val loggingEventService: EventServiceApi

    val isGooglePlayServiceAvailable: Boolean

    val coreCompletionHandlerRefreshTokenProxyProvider: CoreCompletionHandlerRefreshTokenProxyProvider
}
