package com.emarsys.config

import com.emarsys.common.feature.InnerFeature
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.Mapper
import com.emarsys.core.Mockable
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.random.RandomProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils
import com.emarsys.core.util.camelToUpperSnakeCase
import com.emarsys.core.util.filterNotNull
import com.emarsys.core.util.getNullableString
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
@Mockable
class RemoteConfigResponseMapper(private val randomProvider: RandomProvider,
                                 private val hardwareIdProvider: HardwareIdProvider) : Mapper<ResponseModel, RemoteConfig> {
    override fun map(responseModel: ResponseModel): RemoteConfig {
        var remoteConfig = RemoteConfig()
        try {
            var remoteConfigJson = JSONObject(responseModel.body)

            extractOverrideJson(remoteConfigJson)?.let {
                val remoteConfigServiceUrlJson = JsonUtils.merge(remoteConfigJson.optJSONObject("serviceUrls"), it.optJSONObject("serviceUrls"))
                val remoteConfigLuckyJson = JsonUtils.merge(remoteConfigJson.optJSONObject("luckyLogger"), it.optJSONObject("luckyLogger"))
                val remoteConfigFeatureJson = JsonUtils.merge(remoteConfigJson.optJSONObject("features"), it.optJSONObject("features"))

                remoteConfigJson = JsonUtils.merge(remoteConfigJson, it)
                remoteConfigJson.put("serviceUrls", remoteConfigServiceUrlJson)
                remoteConfigJson.put("luckyLogger", remoteConfigLuckyJson)
                remoteConfigJson.put("features", remoteConfigFeatureJson)
            }
            remoteConfig = mapJsonToRemoteConfig(remoteConfigJson)
        } catch (jsonException: JSONException) {
            Logger.error(CrashLog(jsonException))
        }
        return remoteConfig
    }

    private fun extractOverrideJson(remoteConfigJson: JSONObject): JSONObject? {
        return remoteConfigJson.optJSONObject("overrides")?.optJSONObject(hardwareIdProvider.provideHardwareId())
    }

    private fun mapJsonToRemoteConfig(remoteConfigJson: JSONObject): RemoteConfig {
        var remoteConfig = RemoteConfig()
        if (remoteConfigJson.has("serviceUrls")) {
            val serviceUrls = remoteConfigJson.getJSONObject("serviceUrls")
            remoteConfig = remoteConfig.copy(
                    eventServiceUrl = validateUrl(serviceUrls.getNullableString("eventService")),
                    clientServiceUrl = validateUrl(serviceUrls.getNullableString("clientService")),
                    deepLinkServiceUrl = validateUrl(serviceUrls.getNullableString("deepLinkService")),
                    inboxServiceUrl = validateUrl(serviceUrls.getNullableString("inboxService")),
                    messageInboxServiceUrl = validateUrl(serviceUrls.getNullableString("messageInboxService")),
                    mobileEngageV2ServiceUrl = validateUrl(serviceUrls.getNullableString("mobileEngageV2Service")),
                    predictServiceUrl = validateUrl(serviceUrls.getNullableString("predictService")))
        }
        remoteConfig = remoteConfig.copy(logLevel = calculateLogLevel(remoteConfigJson))
        remoteConfig = remoteConfig.copy(features = extractFeatures(remoteConfigJson))
        return remoteConfig
    }

    private fun validateUrl(url: String?): String? {
        var validatedUrl: String? = null
        if (url != null) {
            val domain = URL(url).host
            if (domain.endsWith(".emarsys.net") || domain.endsWith(".emarsys.com")) {
                validatedUrl = url.toString()
            }
        }
        return validatedUrl
    }

    private fun calculateLogLevel(jsonResponse: JSONObject): LogLevel? {
        val luckyLogger = jsonResponse.optJSONObject("luckyLogger")
        var logLevelString = jsonResponse.optString("logLevel")

        if (luckyLogger != null) {
            val threshold = luckyLogger.getDouble("threshold")
            val randomValue = randomProvider.provideDouble(1.0)
            if (randomValue <= threshold && threshold > 0) {
                logLevelString = luckyLogger.getString("logLevel")
            }
        }

        return convertLogLevelStringToLogLevel(logLevelString)
    }

    private fun convertLogLevelStringToLogLevel(logLevel: String): LogLevel? {
        return when (logLevel.lowercase(Locale.ENGLISH)) {
            "trace" -> LogLevel.TRACE
            "debug" -> LogLevel.DEBUG
            "info" -> LogLevel.INFO
            "warn" -> LogLevel.WARN
            "error" -> LogLevel.ERROR
            "metric" -> LogLevel.METRIC
            else -> null
        }
    }

    private fun extractFeatures(remoteConfigJson: JSONObject): Map<InnerFeature, Boolean>? {
        val featuresJson = remoteConfigJson.optJSONObject("features")
        return featuresJson
                ?.keys()
                ?.asSequence()
                ?.associateBy({ InnerFeature.safeValueOf(it.camelToUpperSnakeCase()) }) { featuresJson.getBoolean(it) }
                ?.filterNotNull()
    }
}
