package com.emarsys.mobileengage.notification.command

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
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
                        val url = inAppDescriptor.optString("url", null)
                        val fileUrl = inAppDescriptor.optString("fileUrl", null)
                        val sid = extractSid(payload)
                        getDependency<Handler>("coreSdkHandler")
                                .post {
                                    var html: String? = null
                                    if (fileUrl != null) {
                                        html = getDependency<FileDownloader>().readFileIntoString(fileUrl)
                                        File(fileUrl).delete()
                                    }
                                    if (html == null && url != null) {
                                        html = getDependency<FileDownloader>().readURLIntoString(url)
                                    }
                                    if (campaignId != null && html != null) {
                                        scheduleInAppDisplay(campaignId, html, sid, url)
                                    }
                                }
                    }
                }
            }
        } catch (ignored: JSONException) {
        }
    }

    private fun scheduleInAppDisplay(campaignId: String, html: String, sid: String?, url: String?) {
        val pushToInAppAction = PushToInAppAction(DependencyInjection.getContainer<MobileEngageDependencyContainer>().getInAppPresenter(), campaignId, html, sid, url,
                getDependency<TimestampProvider>())
        getDependency<ActivityLifecycleWatchdog>().addTriggerOnActivityAction(pushToInAppAction)
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