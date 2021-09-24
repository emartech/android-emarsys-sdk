package com.emarsys.mobileengage.notification.command

import android.content.Intent
import android.os.Bundle
import com.emarsys.core.util.getNullableString
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.PushToInAppAction
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class PreloadedInappHandlerCommand(private val intent: Intent) : Runnable {

    override fun run() {
        try {
            val extras = intent.extras
            if (extras != null) {
                val payload = extras.getBundle("payload")
                if (payload != null) {
                    val ems = payload.getString("ems")
                    if (ems != null) {
                        val emsJson = JSONObject(ems)
                        val inAppDescriptor = JSONObject(emsJson.getString("inapp"))
                        val campaignId = inAppDescriptor.getString("campaignId")
                        val url = inAppDescriptor.getNullableString("url")
                        val fileUrl = inAppDescriptor.getNullableString("fileUrl")
                        val sid = extractSid(payload)
                        var html: String? = null
                        if (fileUrl != null) {
                            html = mobileEngage().fileDownloader.readFileIntoString(fileUrl)
                            File(fileUrl).delete()
                        }
                        if (html == null && url != null) {
                            html = mobileEngage().fileDownloader.readURLIntoString(url)
                        }
                        if (campaignId != null && html != null) {
                            scheduleInAppDisplay(campaignId, html, sid, url)
                        }
                    }
                }
            }
        } catch (ignored: JSONException) {
        }
    }

    private fun scheduleInAppDisplay(campaignId: String, html: String, sid: String?, url: String?) {
        val pushToInAppAction = PushToInAppAction(mobileEngage().overlayInAppPresenter, campaignId, html, sid, url,
                mobileEngage().timestampProvider)
        val activityProvider = mobileEngage().currentActivityProvider
        val currentActivity = activityProvider.get()
        if (currentActivity == null) {
            mobileEngage().activityLifecycleActionRegistry.addTriggerOnActivityAction(pushToInAppAction)
        } else {
            pushToInAppAction.execute(currentActivity)
        }
    }

    private fun extractSid(bundle: Bundle?): String? {
        var sid: String? = null
        if (bundle != null && bundle.containsKey("u")) {
            try {
                sid = JSONObject(bundle.getString("u")!!).getString("sid")
            } catch (ignore: JSONException) {
            }
        }
        return sid
    }

}