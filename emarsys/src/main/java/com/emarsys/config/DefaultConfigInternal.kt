package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.feature.InnerFeature
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.predict.request.PredictRequestContext

class DefaultConfigInternal(private val mobileEngageRequestContext: MobileEngageRequestContext,
                            private val mobileEngageInternal: MobileEngageInternal,
                            private val pushInternal: PushInternal,
                            private val pushTokenProvider: PushTokenProvider,
                            private val predictRequestContext: PredictRequestContext) : ConfigInternal {

    override val applicationCode: String?
        get() = mobileEngageRequestContext.applicationCodeStorage.get()

    override val merchantId: String?
        get() = predictRequestContext.merchantId

    override val contactFieldId: Int
        get() = mobileEngageRequestContext.contactFieldId


    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        val originalContactFieldValue = mobileEngageRequestContext.contactFieldValueStorage.get()
        mobileEngageInternal.clearContact {
            if (it == null) {
                if (applicationCode != null) {
                    FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
                    updateApplicationCode(applicationCode, originalContactFieldValue, completionListener)
                } else {
                    FeatureRegistry.disableFeature(InnerFeature.MOBILE_ENGAGE)
                    completionListener?.onCompleted(null)
                }
            } else {
                handleError(it, completionListener)
            }
        }
    }

    private fun updateApplicationCode(applicationCode: String?, originalContactFieldValue: String?, completionListener: CompletionListener?) {
        mobileEngageRequestContext.applicationCodeStorage.set(applicationCode)
        pushInternal.setPushToken(pushTokenProvider.providePushToken()) {
            if (it == null) {
                mobileEngageInternal.setContact(originalContactFieldValue, completionListener)
            } else {
                handleError(it, completionListener)
            }
        }
    }

    private fun handleError(throwable: Throwable?, completionListener: CompletionListener?) {
        completionListener?.onCompleted(throwable)
    }

    override fun changeMerchantId(merchantId: String?) {
        predictRequestContext.merchantId = merchantId
        if (merchantId == null) {
            FeatureRegistry.disableFeature(InnerFeature.PREDICT)
        } else {
            FeatureRegistry.enableFeature(InnerFeature.PREDICT)
        }
    }
}
