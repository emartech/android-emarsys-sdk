package com.emarsys.mobileengage.iam

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.provider.Gettable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import org.json.JSONObject
import java.util.*

@Mockable
class OverlayInAppPresenter(
        private val coreSdkHandler: Handler,
        private val uiHandler: Handler,
        private val webViewProvider: IamStaticWebViewProvider,
        private val inAppInternal: InAppInternal,
        private val dialogProvider: IamDialogProvider,
        private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
        private val displayedIamRepository: Repository<DisplayedIam, SqlSpecification>,
        private val timestampProvider: TimestampProvider,
        private val currentActivityProvider: Gettable<Activity>,
        private val jsBridgeFactory: IamJsBridgeFactory) {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun present(campaignId: String?, sid: String?, url: String?, requestId: String?, startTimestamp: Long, html: String?, messageLoadedListener: MessageLoadedListener?) {
        val iamDialog = dialogProvider.provideDialog(campaignId, sid, url, requestId)
        setupDialogWithActions(iamDialog)

        val jsBridge = jsBridgeFactory.createJsBridge()

        jsBridge.onAppEventListener = onAppEventTriggered()

        jsBridge.onCloseListener = onCloseTriggered()

        jsBridge.onButtonClickedListener = onButtonClickedTriggered(campaignId, sid, url)

        jsBridge.onOpenExternalUrlListener = onExternalUrlTriggered()

        jsBridge.onMEEventListener = onMEEventTriggered()


        webViewProvider.loadMessageAsync(html, jsBridge) {
            val currentActivity = currentActivityProvider.get()
            val endTimestamp = timestampProvider.provideTimestamp()
            iamDialog.setInAppLoadingTime(InAppLoadingTime(startTimestamp, endTimestamp))
            if (currentActivity is AppCompatActivity) {
                val fragmentManager = currentActivity.supportFragmentManager
                val fragment = fragmentManager.findFragmentByTag(IamDialog.TAG)
                if (fragment == null) {
                    iamDialog.show(fragmentManager, IamDialog.TAG)
                }
            }
            messageLoadedListener?.onMessageLoaded()
        }
    }

    fun onExternalUrlTriggered(): ((property: String?, json: JSONObject) -> Unit) {
        return { property, _ ->
            val activity = currentActivityProvider.get()
            if (activity != null) {
                val link = Uri.parse(property)
                val intent = Intent(Intent.ACTION_VIEW, link)
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                } else {
                    throw Exception("Url cannot be handled by any application!")
                }
            } else {
                throw Exception("UI unavailable!")
            }
        }
    }

    fun onButtonClickedTriggered(campaignId: String?, sid: String?, url: String?): ((property: String?, json: JSONObject) -> Unit) {
        return { property, _ ->
            buttonClickedRepository.add(ButtonClicked(campaignId, property, System.currentTimeMillis()))
            val eventName = "inapp:click"
            val attributes: MutableMap<String, String?> = HashMap()
            attributes["campaignId"] = campaignId
            attributes["buttonId"] = property
            if (sid != null) {
                attributes["sid"] = sid
            }
            if (url != null) {
                attributes["url"] = url
            }
            inAppInternal.trackInternalCustomEvent(eventName, attributes, null)
        }
    }

    fun onCloseTriggered(): () -> Unit {
        return {
            val currentActivity = currentActivityProvider.get()
            if (currentActivity is AppCompatActivity) {
                val fragment = currentActivity.supportFragmentManager.findFragmentByTag(IamDialog.TAG)
                if (fragment is DialogFragment) {
                    fragment.dismiss()
                }
            }
        }
    }

    fun onMEEventTriggered(): (String?, JSONObject) -> Unit {
        return { property, json ->
            coreSdkHandler.post {
                val attributes = extractAttributes(json)
                inAppInternal.trackCustomEventAsync(property, attributes, null)
            }
        }
    }

    fun onAppEventTriggered(): (String?, JSONObject) -> Unit {
        return { property, json ->
            val payload = json.optJSONObject("payload")
            val currentActivity = currentActivityProvider.get()
            if (property != null) {
                uiHandler.post {
                    inAppInternal.eventHandler?.handleEvent(currentActivity, property, payload)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setupDialogWithActions(iamDialog: IamDialog) {
        val saveDisplayedIamAction: OnDialogShownAction = SaveDisplayedIamAction(
                coreSdkHandler,
                displayedIamRepository,
                timestampProvider)
        val sendDisplayedIamAction: OnDialogShownAction = SendDisplayedIamAction(
                coreSdkHandler,
                inAppInternal)
        iamDialog.setActions(listOf(saveDisplayedIamAction, sendDisplayedIamAction))
    }

    private fun extractAttributes(json: JSONObject): Map<String, String>? {
        val payload = json.optJSONObject("payload")
        return payload?.keys()?.asSequence()?.associateBy({ it }, { payload.getString(it) })
    }
}