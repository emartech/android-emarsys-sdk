package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.emarsys.core.Mockable
import com.emarsys.core.notification.NotificationSettings
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import java.text.SimpleDateFormat
import java.util.*

@Mockable
data class DeviceInfo(private val context: Context,
                      private val hardwareIdProvider: HardwareIdProvider,
                      private val versionProvider: VersionProvider,
                      private val languageProvider: LanguageProvider,
                      val notificationSettings: NotificationSettings,
                      val isAutomaticPushSendingEnabled: Boolean) {

    companion object {
        const val UNKNOWN_VERSION_NAME = "unknown"
    }

    val hwid: String = hardwareIdProvider.provideHardwareId()
    val platform: String = "android"
    val language: String = languageProvider.provideLanguage(Locale.getDefault())
    val timezone: String = SimpleDateFormat("Z", Locale.ENGLISH).format(Calendar.getInstance().time)
    val manufacturer: String = Build.MANUFACTURER
    val model: String = Build.MODEL
    val osVersion: String = Build.VERSION.RELEASE
    val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
    val isDebugMode: Boolean = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
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

    val hash: Int
        get() = hashCode()
}