package com.emarsys.di

import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.feature.InnerFeature
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.push.PushApi

object EmarsysDependencyInjection : DependencyInjection() {

    @JvmStatic
    fun mobileEngageInternal(): MobileEngageInternal {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun predictInternal(): PredictInternal {
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
    fun deepLinkInternal(): DeepLinkInternal {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun clientServiceInternal(): ClientServiceInternal {
        return if (isMobileEngageEnabled()) {
            getDependency("defaultInstance")
        } else {
            getDependency("loggingInstance")
        }
    }

    @JvmStatic
    fun eventServiceInternal(): EventServiceInternal {
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