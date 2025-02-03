package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.emarsys.core.Mockable
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.util.AndroidVersionUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Mockable
data class DeviceInfo(
    private val context: Context,
    private val clientIdProvider: ClientIdProvider,
    private val versionProvider: VersionProvider,
    private val languageProvider: LanguageProvider,
    val notificationSettings: NotificationSettings,
    val isAutomaticPushSendingEnabled: Boolean,
    val isGooglePlayAvailable: Boolean
) {

    companion object {
        const val UNKNOWN_VERSION_NAME = "unknown"
    }

    val clientId: String = clientIdProvider.provideClientId()
    val platform: String
        get() {
            return if (isGooglePlayAvailable) "android" else "android-huawei"
        }
    val language: String = languageProvider.provideLanguage(Locale.getDefault())
    val timezone: String = SimpleDateFormat("Z", Locale.ENGLISH).format(Calendar.getInstance().time)
    val manufacturer: String = Build.MANUFACTURER
    val model: String = Build.MODEL
    val osVersion: String = Build.VERSION.RELEASE
    val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
    val isDebugMode: Boolean =
        0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    var sdkVersion: String = versionProvider.provideSdkVersion()
    val applicationVersion: String?
        get() {
            var version: String? = null
            try {
                version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (ignored: PackageManager.NameNotFoundException) {
            }
            if (version == null) {
                version = UNKNOWN_VERSION_NAME
            }
            return version
        }

    val deviceInfoPayload: String
        get() = JSONObject(
            mapOf(
                "notificationSettings" to mapOf(
                    parseChannelSettings(),
                    "importance" to notificationSettings.importance,
                    "areNotificationsEnabled" to notificationSettings.areNotificationsEnabled
                ),
                "hwid" to clientId,
                "platform" to platform,
                "language" to language,
                "timezone" to timezone,
                "manufacturer" to manufacturer,
                "model" to model,
                "osVersion" to osVersion,
                "displayMetrics" to "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
                "sdkVersion" to sdkVersion,
                "appVersion" to applicationVersion
            )
        ).toString()

    private fun parseChannelSettings(): Pair<String, Any> {
        return if (AndroidVersionUtils.isOreoOrAbove) {
            "channelSettings" to notificationSettings.channelSettings.map {
                JSONObject(
                    mapOf(
                        "channelId" to it.channelId,
                        "importance" to it.importance,
                        "isCanBypassDnd" to it.isCanBypassDnd,
                        "isCanShowBadge" to it.isCanShowBadge,
                        "isShouldVibrate" to it.isShouldVibrate
                    )
                )
            }
        } else {
            "channelSettings" to listOf(JSONObject())
        }
    }
}