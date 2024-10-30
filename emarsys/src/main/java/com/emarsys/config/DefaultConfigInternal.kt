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
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.predict.request.PredictRequestContext
import java.util.concurrent.CountDownLatch

@Mockable
class DefaultConfigInternal(
    private val mobileEngageRequestContext: MobileEngageRequestContext,
    private val mobileEngageInternal: MobileEngageInternal,
    private val pushInternal: PushInternal,
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
    private val clientServiceInternal: ClientServiceInternal,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) : ConfigInternal {

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


    override fun changeApplicationCode(
        applicationCode: String?,
        completionListener: CompletionListener?
    ) {
        val pushToken: String? = pushInternal.pushToken
        val hasContactIdentification = mobileEngageRequestContext.hasContactIdentification()
        var throwable: Throwable? = null
        concurrentHandlerHolder.postOnBackground {

            if (pushToken != null && mobileEngageRequestContext.applicationCode != null) {
                throwable = clearPushToken()
            }
            if ((throwable == null) && (mobileEngageRequestContext.applicationCode != null) && hasContactIdentification) {
                throwable = clearContact()
            }
            if (throwable == null) {
                handleAppCodeChange(applicationCode)
                if (applicationCode != null) {
                    throwable = sendDeviceInfo()
                    if (pushToken != null) {
                        throwable = sendPushToken(pushToken)
                    }
                    if (throwable == null && !hasContactIdentification) {
                        throwable = clearContact()
                    }
                }
            }
            if (throwable != null) {
                handleAppCodeChange(null)
            }

            concurrentHandlerHolder.postOnMain {
                completionListener?.onCompleted(throwable)
            }
        }
    }

    private fun synchronizeMethodWithRunnerCallback(runnerCallback: (CompletionListener) -> (Unit)): Throwable? {
        var result: Throwable? = null
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            result = it
            latch.countDown()
        }
        runnerCallback(completionListener)
        latch.await()
        return result
    }

    private fun handleAppCodeChange(applicationCode: String?) {
        mobileEngageRequestContext.reset()
        mobileEngageRequestContext.applicationCode = applicationCode
        if (applicationCode != null) {
            FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
            FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)
        } else {
            FeatureRegistry.disableFeature(InnerFeature.MOBILE_ENGAGE)
            FeatureRegistry.disableFeature(InnerFeature.EVENT_SERVICE_V4)
        }
    }

    private fun clearPushToken(): Throwable? {
        return synchronizeMethodWithRunnerCallback {
            pushInternal.clearPushToken(it)
        }
    }

    private fun clearContact(): Throwable? {
        return synchronizeMethodWithRunnerCallback {
            mobileEngageInternal.clearContact(it)
        }
    }

    private fun sendPushToken(pushToken: String): Throwable? {
        return synchronizeMethodWithRunnerCallback {
            pushInternal.setPushToken(pushToken, it)
        }
    }

    private fun sendDeviceInfo(): Throwable? {
        return synchronizeMethodWithRunnerCallback {
            clientServiceInternal.trackDeviceInfo(it)
        }
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
                            if (crypto.verify(
                                    remoteConfigResponseModel.body!!.toByteArray(),
                                    signature
                                )
                            ) {
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
                val response = Try.failure<String>(
                    ResponseErrorException(
                        responseModel.statusCode,
                        responseModel.message,
                        responseModel.body
                    )
                )

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
                val response = Try.failure<ResponseModel>(
                    ResponseErrorException(
                        responseModel.statusCode,
                        responseModel.message,
                        responseModel.body
                    )
                )

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
