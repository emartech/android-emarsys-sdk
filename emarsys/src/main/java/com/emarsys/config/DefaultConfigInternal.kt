package com.emarsys.config

import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
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
                            private val emarsysRequestModelFactory: EmarsysRequestModelFactory,
                            private val configResponseMapper: RemoteConfigResponseMapper,
                            private val clientServiceStorage: Storage<String>,
                            private val eventServiceStorage: Storage<String>,
                            private val deeplinkServiceStorage: Storage<String>,
                            private val inboxServiceStorage: Storage<String>,
                            private val mobileEngageV2ServiceStorage: Storage<String>,
                            private val predictServiceStorage: Storage<String>,
                            private val messageInboxServiceStorage: Storage<String>,
                            private val logLevelStorage: Storage<String>) : ConfigInternal {

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

    private var originalContactFieldId: Int = 0

    override fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: CompletionListener?) {
        originalContactFieldId = mobileEngageRequestContext.contactFieldId
        val contactFieldIdHasChanged = contactFieldId != originalContactFieldId

        val originalContactFieldValue = mobileEngageRequestContext.contactFieldValueStorage.get()
        resetRemoteConfig()
        val completionListenerWrapper = wrapCompletionListenerWithRefreshRemoteConfig(completionListener)
        if (mobileEngageRequestContext.applicationCode == null) {
            changeApplicationCode(applicationCode, originalContactFieldValue, contactFieldIdHasChanged, contactFieldId, completionListenerWrapper)
        } else {
            clearCurrentContact(completionListenerWrapper) {
                changeApplicationCode(applicationCode, originalContactFieldValue, contactFieldIdHasChanged, contactFieldId, completionListenerWrapper)
            }
        }
    }

    private fun wrapCompletionListenerWithRefreshRemoteConfig(completionListener: CompletionListener?): CompletionListener {
        return CompletionListener {
            completionListener?.onCompleted(it)
            if (FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)) {
                refreshRemoteConfig()
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

    private fun changeApplicationCode(applicationCode: String?, originalContactFieldValue: String?, contactFieldIdHasChanged: Boolean, newContactFieldId: Int, completionListener: CompletionListener?) {
        if (applicationCode != null) {
            FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
            mobileEngageRequestContext.applicationCode = applicationCode
            mobileEngageRequestContext.contactFieldId = newContactFieldId
            updatePushToken(completionListener) {
                if (!contactFieldIdHasChanged) {
                    setPreviouslyLoggedInContact(originalContactFieldValue, completionListener)
                } else {
                    completionListener?.onCompleted(null)
                }
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
        mobileEngageRequestContext.contactFieldId = originalContactFieldId
        completionListener?.onCompleted(throwable)
    }

    override fun changeMerchantId(merchantId: String?) {
        resetRemoteConfig()
        predictRequestContext.merchantId = merchantId
        if (merchantId == null) {
            FeatureRegistry.disableFeature(InnerFeature.PREDICT)
        } else {
            FeatureRegistry.enableFeature(InnerFeature.PREDICT)
            refreshRemoteConfig()
        }
    }

    override fun refreshRemoteConfig() {
        fetchRemoteConfig(ResultListener {
            it.result?.let { remoteConfig ->
                applyRemoteConfig(remoteConfig)
            }
            it.errorCause?.let {
                resetRemoteConfig()
            }
        })
    }

    fun fetchRemoteConfig(resultListener: ResultListener<Try<RemoteConfig>>) {
        val requestModel = emarsysRequestModelFactory.createRemoteConfigRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String?, responseModel: ResponseModel?) {
                val remoteConfig = Try.success(configResponseMapper.map(responseModel))

                resultListener.onResult(remoteConfig)
            }

            override fun onError(id: String?, responseModel: ResponseModel?) {
                val response = Try.failure<RemoteConfig>(ResponseErrorException(
                        responseModel!!.statusCode,
                        responseModel.message,
                        responseModel.body))

                resultListener.onResult(response)
            }

            override fun onError(id: String?, cause: Exception?) {
                val response = Try.failure<RemoteConfig>(cause)

                resultListener.onResult(response)
            }
        })
    }

    fun applyRemoteConfig(remoteConfig: RemoteConfig) {
        clientServiceStorage.set(remoteConfig.clientServiceUrl)
        eventServiceStorage.set(remoteConfig.eventServiceUrl)
        deeplinkServiceStorage.set(remoteConfig.deepLinkServiceUrl)
        inboxServiceStorage.set(remoteConfig.inboxServiceUrl)
        mobileEngageV2ServiceStorage.set(remoteConfig.mobileEngageV2ServiceUrl)
        predictServiceStorage.set(remoteConfig.predictServiceUrl)
        messageInboxServiceStorage.set(remoteConfig.messageInboxServiceUrl)
        logLevelStorage.set(remoteConfig.logLevel?.name)
    }

    override fun resetRemoteConfig() {
        clientServiceStorage.set(null)
        eventServiceStorage.set(null)
        deeplinkServiceStorage.set(null)
        inboxServiceStorage.set(null)
        mobileEngageV2ServiceStorage.set(null)
        predictServiceStorage.set(null)
        messageInboxServiceStorage.set(null)
        logLevelStorage.set(null)
    }
}
