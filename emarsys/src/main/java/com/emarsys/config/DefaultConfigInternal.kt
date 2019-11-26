package com.emarsys.config

import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.notification.NotificationSettings
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.feature.InnerFeature
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.predict.request.PredictRequestContext

@Mockable
class DefaultConfigInternal(private val mobileEngageRequestContext: MobileEngageRequestContext,
                            private val mobileEngageInternal: MobileEngageInternal,
                            private val pushInternal: PushInternal,
                            private val pushTokenProvider: PushTokenProvider,
                            private val predictRequestContext: PredictRequestContext,
                            private val deviceInfo: DeviceInfo,
                            private val requestManager: RequestManager,
                            private val emarsysRequestModelFactory: EmarsysRequestModelFactory) : ConfigInternal {

    override val applicationCode: String?
        get() = mobileEngageRequestContext.applicationCode

    override val merchantId: String?
        get() = predictRequestContext.merchantId

    override val contactFieldId: Int
        get() = mobileEngageRequestContext.contactFieldId

    override val hardwareId: String
        get() = deviceInfo.hwid

    override val language: String
        get() = deviceInfo.language

    override val notificationSettings: NotificationSettings
        get() = deviceInfo.notificationSettings


    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        val originalContactFieldValue = mobileEngageRequestContext.contactFieldValueStorage.get()

        if (mobileEngageRequestContext.applicationCode == null) {
            changeApplicationCode(applicationCode, originalContactFieldValue, completionListener)
        } else {
            clearCurrentContact(completionListener) {
                changeApplicationCode(applicationCode, originalContactFieldValue, completionListener)
            }
        }
    }

    private fun clearCurrentContact(completionListener: CompletionListener?, onSuccess: () -> Unit) {
        mobileEngageInternal.clearContact {
            if (it == null) {
                onSuccess()
            } else {
                handleError(it, completionListener)
            }
        }
    }

    private fun changeApplicationCode(applicationCode: String?, originalContactFieldValue: String?, completionListener: CompletionListener?) {
        if (applicationCode != null) {
            FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
            mobileEngageRequestContext.applicationCode = applicationCode
            updatePushToken(completionListener) {
                setPreviouslyLoggedInContact(originalContactFieldValue, completionListener)
            }
        } else {
            FeatureRegistry.disableFeature(InnerFeature.MOBILE_ENGAGE)
            completionListener?.onCompleted(null)
        }
    }

    private fun updatePushToken(completionListener: CompletionListener?, onSuccess: () -> Unit) {
        val pushToken = pushTokenProvider.providePushToken()
        if (pushToken != null) {
            pushInternal.setPushToken(pushToken) {
                if (it == null) {
                    onSuccess()
                } else {
                    handleError(it, completionListener)
                }
            }
        } else {
            onSuccess()
        }
    }

    private fun setPreviouslyLoggedInContact(originalContactFieldValue: String?, completionListener: CompletionListener?) {
        mobileEngageInternal.setContact(originalContactFieldValue) {
            if (it != null) {
                handleError(it, completionListener)
            } else {
                completionListener?.onCompleted(it)
            }
        }
    }

    private fun handleError(throwable: Throwable?, completionListener: CompletionListener?) {
        FeatureRegistry.disableFeature(InnerFeature.MOBILE_ENGAGE)
        mobileEngageRequestContext.applicationCode = null
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

    override fun fetchRemoteConfig() {
        val requestModel = emarsysRequestModelFactory.createRemoteConfigRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String?, responseModel: ResponseModel?) {
            }

            override fun onError(id: String?, responseModel: ResponseModel?) {
            }

            override fun onError(id: String?, cause: Exception?) {
            }
        })
    }
}
