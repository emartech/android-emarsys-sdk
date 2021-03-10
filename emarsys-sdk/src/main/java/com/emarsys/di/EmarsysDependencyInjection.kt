package com.emarsys.di

import com.emarsys.clientservice.ClientServiceApi
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
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

object EmarsysDependencyInjection : DependencyInjection() {

    @JvmStatic
    fun mobileEngageApi(): MobileEngageApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun predictApi(): PredictApi {
        return if (isPredictEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun predictRestrictedApi(): PredictRestrictedApi {
        return if (isPredictEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun inbox(): InboxApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun inApp(): InAppApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun onEventAction(): OnEventActionApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun deepLinkApi(): DeepLinkApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun clientServiceApi(): ClientServiceApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun eventServiceApi(): EventServiceApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun push(): PushApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun predict(): PredictApi {
        return if (isPredictEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun messageInbox(): MessageInboxApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun geofence(): GeofenceApi {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    private fun isMobileEngageEnabled(): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)
    }

    private fun isPredictEnabled(): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT)
    }
}