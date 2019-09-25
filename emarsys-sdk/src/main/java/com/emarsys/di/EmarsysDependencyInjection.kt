package com.emarsys.di

import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.feature.InnerFeature
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
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
            container().mobileEngageInternal
        } else {
            container().loggingMobileEngageInternal
        }
    }

    @JvmStatic
    fun predictInternal(): PredictInternal {
        return if (isPredictEnabled()) {
            container().predictInternal
        } else {
            container().loggingPredictInternal
        }
    }

    @JvmStatic
    fun inbox(): InboxApi {
        return if (isMobileEngageEnabled()) {
            container().inbox
        } else {
            container().loggingInbox
        }
    }

    @JvmStatic
    fun inApp(): InAppApi {
        return if (isMobileEngageEnabled()) {
            container().inApp
        } else {
            container().loggingInApp
        }
    }

    @JvmStatic
    fun deepLinkInternal(): DeepLinkInternal {
        return if (isMobileEngageEnabled()) {
            container().deepLinkInternal
        } else {
            container().loggingDeepLinkInternal
        }
    }

    @JvmStatic
    fun clientServiceInternal(): ClientServiceInternal {
        return if (isMobileEngageEnabled()) {
            container().clientServiceInternal
        } else {
            container().loggingClientServiceInternal
        }
    }

    @JvmStatic
    fun eventServiceInternal(): EventServiceInternal {
        return if (isMobileEngageEnabled()) {
            container().eventServiceInternal
        } else {
            container().loggingEventServiceInternal
        }
    }

    @JvmStatic
    fun push(): PushApi {
        return if (isMobileEngageEnabled()) {
            container().push
        } else {
            container().loggingPush
        }
    }

    @JvmStatic
    fun predict(): PredictApi {
        return if (isPredictEnabled()) {
            container().predict
        } else {
            container().loggingPredict
        }
    }

    private fun isMobileEngageEnabled(): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)
    }

    private fun isPredictEnabled(): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT)
    }

    private fun container(): EmarysDependencyContainer {
        checkNotNull(container) { "DependencyInjection must be setup before accessing container!" }
        return container as EmarysDependencyContainer
    }
}