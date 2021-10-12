package com.emarsys.config

import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.common.feature.InnerFeature
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.client.ClientServiceInternal
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
                            private val clientServiceStorage: Storage<String?>,
                            private val eventServiceStorage: Storage<String?>,
                            private val deeplinkServiceStorage: Storage<String?>,
                            private val predictServiceStorage: Storage<String?>,
                            private val messageInboxServiceStorage: Storage<String?>,
                            private val logLevelStorage: Storage<String?>,
                            private val crypto: Crypto,
                            private val clientServiceInternal: ClientServiceInternal) : ConfigInternal {

    override val applicationCode: String?
        get() = mobileEngageRequestContext.applicationCode

    override val merchantId: String?
        get() = predictRequestContext.merchantId

    override val contactFieldId: Int?
        get() = mobileEngageRequestContext.contactFieldId

    override val hardwareId: String
        get() = deviceInfo.hardwareId

    override val language: String
        get() = deviceInfo.language

    override val notificationSettings: NotificationSettings
        get() = deviceInfo.notificationSettings

    override val isAutomaticPushSendingEnabled: Boolean
        get() = deviceInfo.isAutomaticPushSendingEnabled

    override val sdkVersion: String
        get() = deviceInfo.sdkVersion

    private var originalPushToken: String? = null

    private var hasContactIdentification: Boolean = false

    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        originalPushToken = pushTokenProvider.providePushToken()
        hasContactIdentification = mobileEngageRequestContext.hasContactIdentification()

        if (mobileEngageRequestContext.applicationCode == null) {
            handleApplicationCodeChange(applicationCode, completionListener)
        } else {
            clearUpPushTokenAndContact(completionListener) {
                handleApplicationCodeChange(applicationCode, completionListener)
            }
        }
    }

    private fun clearUpPushTokenAndContact(completionListener: CompletionListener?, onSuccess: () -> Unit) {
        clearPushToken(completionListener) {
            clearContact(completionListener) {
                onSuccess()
            }
        }
    }

    private fun clearContactIfWasNotIdentified(completionListener: CompletionListener?, onSuccess: () -> Unit) {
            if (hasContactIdentification) {
                onSuccess()
            } else {
                clearContact(completionListener, onSuccess)
            }
    }

    private fun clearContact(completionListener: CompletionListener?, onSuccess: () -> Unit) {
        mobileEngageInternal.clearContact { throwable ->
            if (throwable == null) {
                onSuccess()
            } else {
                handleError(throwable, completionListener)
            }
        }
    }

    private fun clearPushToken(completionListener: CompletionListener?, onSuccess: () -> Unit) {
        if (originalPushToken != null) {
            pushInternal.clearPushToken { throwable ->
                if (throwable == null) {
                    onSuccess()
                } else {
                    handleError(throwable, completionListener)
                }
            }
        } else {
            onSuccess()
        }
    }

    private fun handleApplicationCodeChange(applicationCode: String?, completionListener: CompletionListener?) {
        if (applicationCode != null) {
            FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
            FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)
            mobileEngageRequestContext.applicationCode = applicationCode
            collectClientState(completionListener) {
                clearContactIfWasNotIdentified(completionListener) {
                    completionListener?.onCompleted(null)
                }
            }
        } else {
            FeatureRegistry.disableFeature(InnerFeature.MOBILE_ENGAGE)
            completionListener?.onCompleted(null)
        }
    }

    private fun collectClientState(completionListener: CompletionListener?, onSuccess: () -> Unit) {
        clientServiceInternal.trackDeviceInfo {
            if (originalPushToken != null) {
                pushInternal.setPushToken(originalPushToken) {
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

    override fun refreshRemoteConfig(completionListener: CompletionListener?) {
        mobileEngageRequestContext.applicationCode?.let {
            fetchRemoteConfigSignature(ResultListener { signatureResponse ->
                signatureResponse.result?.let { signature ->
                    fetchRemoteConfig(ResultListener {
                        it.result?.let { remoteConfigResponseModel ->
                            if (crypto.verify(remoteConfigResponseModel.body!!.toByteArray(), signature)) {
                                applyRemoteConfig(configResponseMapper.map(remoteConfigResponseModel))
                                completionListener?.onCompleted(null)
                            } else {
                                resetRemoteConfig()
                                completionListener?.onCompleted(Exception("Verify failed"))
                            }
                        }
                        it.errorCause?.let { throwable ->
                            resetRemoteConfig()
                            completionListener?.onCompleted(throwable)

                        }
                    })
                }
                signatureResponse.errorCause?.let { throwable ->
                    resetRemoteConfig()
                    completionListener?.onCompleted(throwable)
                }
            })
        }
    }

    fun fetchRemoteConfigSignature(resultListener: ResultListener<Try<String>>) {
        val requestModel = emarsysRequestModelFactory.createRemoteConfigSignatureRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val remoteConfigSignature = Try.success(responseModel.body!!)

                resultListener.onResult(remoteConfigSignature)
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                val response = Try.failure<String>(ResponseErrorException(
                        responseModel.statusCode,
                        responseModel.message,
                        responseModel.body))

                resultListener.onResult(response)
            }

            override fun onError(id: String, cause: Exception) {
                val response = Try.failure<String>(cause)

                resultListener.onResult(response)
            }
        })
    }

    fun fetchRemoteConfig(resultListener: ResultListener<Try<ResponseModel>>) {
        val requestModel = emarsysRequestModelFactory.createRemoteConfigRequest()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val remoteConfig = Try.success(responseModel)

                resultListener.onResult(remoteConfig)
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                val response = Try.failure<ResponseModel>(ResponseErrorException(
                        responseModel.statusCode,
                        responseModel.message,
                        responseModel.body))

                resultListener.onResult(response)
            }

            override fun onError(id: String, cause: Exception) {
                val response = Try.failure<ResponseModel>(cause)

                resultListener.onResult(response)
            }
        })
    }

    fun applyRemoteConfig(remoteConfig: RemoteConfig) {
        clientServiceStorage.set(remoteConfig.clientServiceUrl)
        eventServiceStorage.set(remoteConfig.eventServiceUrl)
        deeplinkServiceStorage.set(remoteConfig.deepLinkServiceUrl)
        predictServiceStorage.set(remoteConfig.predictServiceUrl)
        messageInboxServiceStorage.set(remoteConfig.messageInboxServiceUrl)
        logLevelStorage.set(remoteConfig.logLevel?.name)
        overrideFeatureFlippers(remoteConfig)
    }

    private fun overrideFeatureFlippers(remoteConfig: RemoteConfig) {
        if (remoteConfig.features != null) {
            InnerFeature.values().forEach {
                if (remoteConfig.features[it] == true) {
                    FeatureRegistry.enableFeature(it)
                } else if (remoteConfig.features[it] == false) {
                    FeatureRegistry.disableFeature(it)
                }
            }
        }
    }

    override fun resetRemoteConfig() {
        clientServiceStorage.set(null)
        eventServiceStorage.set(null)
        deeplinkServiceStorage.set(null)
        predictServiceStorage.set(null)
        messageInboxServiceStorage.set(null)
        logLevelStorage.set(null)
    }
}
