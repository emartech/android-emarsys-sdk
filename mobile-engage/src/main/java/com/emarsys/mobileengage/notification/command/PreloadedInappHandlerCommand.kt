package com.emarsys.mobileengage.notification.command

import com.emarsys.core.util.getNullableString
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.PushToInAppAction
import com.emarsys.mobileengage.service.NotificationData
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class PreloadedInappHandlerCommand(private val notificationData: NotificationData) : Runnable {

    override fun run() {
        try {
            if (notificationData.inapp != null) {
                val inAppDescriptor = JSONObject(notificationData.inapp)
                val campaignId = inAppDescriptor.getNullableString("campaignId")
                val url = inAppDescriptor.getNullableString("url")
                val fileUrl = inAppDescriptor.getNullableString("fileUrl")
                var html: String? = null
                if (fileUrl != null) {
                    html = mobileEngage().fileDownloader.readFileIntoString(fileUrl)
                    File(fileUrl).delete()
                }
                if (html == null && url != null) {
                    html = mobileEngage().fileDownloader.readURLIntoString(url)
                }
                if (campaignId != null && html != null) {
                    scheduleInAppDisplay(campaignId, html, notificationData.sid, url)
                }
            }
        } catch (ignored: JSONException) {
        }
    }

    private fun scheduleInAppDisplay(campaignId: String, html: String, sid: String?, url: String?) {
        val pushToInAppAction = PushToInAppAction(
            mobileEngage().overlayInAppPresenter, campaignId, html, sid, url,
            mobileEngage().timestampProvider
        )
        val activityProvider = mobileEngage().currentActivityProvider
        val currentActivity = activityProvider.get()
        if (currentActivity == null) {
            mobileEngage().activityLifecycleActionRegistry.addTriggerOnActivityAction(
                pushToInAppAction
            )
        } else {
            pushToInAppAction.execute(currentActivity)
        }
    }
}