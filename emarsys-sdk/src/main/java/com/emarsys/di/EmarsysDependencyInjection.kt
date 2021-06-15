package com.emarsys.di

import com.emarsys.clientservice.ClientServiceApi
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageApi
import com.emarsys.oneventaction.OnEventActionApi
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictRestrictedApi
import com.emarsys.push.PushApi

object EmarsysDependencyInjection {

    @JvmStatic
    fun mobileEngageApi(): MobileEngageApi {
        return if (isMobileEngageEnabled()) {
            emarsys().mobileEngage
        } else {
            emarsys().loggingMobileEngage
        }
    }

    @JvmStatic
    fun predictRestrictedApi(): PredictRestrictedApi {
        return if (isPredictEnabled()) {
            emarsys().predictRestricted
        } else {
            emarsys().loggingPredictRestricted
        }
    }

    @JvmStatic
    fun inbox(): InboxApi {
        return if (isMobileEngageEnabled()) {
            emarsys().inbox
        } else {
            emarsys().loggingInbox
        }
    }

    @JvmStatic
    fun inApp(): InAppApi {
        return if (isMobileEngageEnabled()) {
            emarsys().inApp
        } else {
            emarsys().loggingInApp
        }
    }

    @JvmStatic
    fun onEventAction(): OnEventActionApi {
        return if (isMobileEngageEnabled()) {
            emarsys().onEventAction
        } else {
            emarsys().loggingOnEventAction
        }
    }

    @JvmStatic
    fun deepLinkApi(): DeepLinkApi {
        return if (isMobileEngageEnabled()) {
            emarsys().deepLink
        } else {
            emarsys().loggingDeepLink
        }
    }

    @JvmStatic
    fun clientServiceApi(): ClientServiceApi {
        return if (isMobileEngageEnabled()) {
            emarsys().clientService
        } else {
            emarsys().loggingClientService
        }
    }

    @JvmStatic
    fun eventServiceApi(): EventServiceApi {
        return if (isMobileEngageEnabled()) {
            emarsys().eventService
        } else {
            emarsys().loggingEventService
        }
    }

    @JvmStatic
    fun push(): PushApi {
        return if (isMobileEngageEnabled()) {
            emarsys().push
        } else {
            emarsys().loggingPush
        }
    }

    @JvmStatic
    fun predict(): PredictApi {
        return if (isPredictEnabled()) {
            emarsys().predict
        } else {
            emarsys().loggingPredict
        }
    }

    @JvmStatic
    fun messageInbox(): MessageInboxApi {
        return if (isMobileEngageEnabled()) {
            emarsys().messageInbox
        } else {
            emarsys().loggingMessageInbox
        }
    }

    @JvmStatic
    fun geofence(): GeofenceApi {
        return if (isMobileEngageEnabled()) {
            emarsys().geofence
        } else {
            emarsys().loggingGeofence
        }
    }

    private fun isMobileEngageEnabled(): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)
    }

    private fun isPredictEnabled(): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT)
    }
}