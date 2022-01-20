package com.emarsys.mobileengage.iam

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.InAppMessage
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import kotlinx.coroutines.launch
import org.json.JSONObject

@Mockable
class OverlayInAppPresenter(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val webViewProvider: IamStaticWebViewProvider,
    private val inAppInternal: InAppInternal,
    private val dialogProvider: IamDialogProvider,
    private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
    private val displayedIamRepository: Repository<DisplayedIam, SqlSpecification>,
    private val timestampProvider: TimestampProvider,
    private val currentActivityProvider: CurrentActivityProvider,
    private val jsBridgeFactory: IamJsBridgeFactory
) {

    fun present(
        campaignId: String, sid: String?, url: String?, requestId: String?, startTimestamp: Long,
        html: String, messageLoadedListener: MessageLoadedListener?
    ) {
        val iamDialog = dialogProvider.provideDialog(campaignId, sid, url, requestId)
        setupDialogWithActions(iamDialog)
        val jsCommandFactory = JSCommandFactory(
            currentActivityProvider, concurrentHandlerHolder, inAppInternal,
            buttonClickedRepository, onCloseTriggered(), onAppEventTriggered(), timestampProvider
        )

        val jsBridge =
            jsBridgeFactory.createJsBridge(jsCommandFactory, InAppMessage(campaignId, sid, url))

        webViewProvider.loadMessageAsync(html, jsBridge) {
            val currentActivity = currentActivityProvider.get()
            val endTimestamp = timestampProvider.provideTimestamp()
            iamDialog.setInAppLoadingTime(InAppLoadingTime(startTimestamp, endTimestamp))
            if (currentActivity is FragmentActivity) {
                val fragmentManager = currentActivity.supportFragmentManager
                val fragment = fragmentManager.findFragmentByTag(IamDialog.TAG)
                if (fragment == null) {
                    iamDialog.show(fragmentManager, IamDialog.TAG)
                }
            }
            messageLoadedListener?.onMessageLoaded()
        }
    }

    fun onCloseTriggered(): OnCloseListener {
        return {
            val currentActivity = currentActivityProvider.get()
            if (currentActivity is FragmentActivity) {
                concurrentHandlerHolder.uiScope.launch {
                    val fragment =
                        currentActivity.supportFragmentManager.findFragmentByTag(IamDialog.TAG)
                    if (fragment is DialogFragment) {
                        fragment.dismiss()
                    }
                }
            }
        }
    }

    fun onAppEventTriggered(): OnAppEventListener {
        return { property: String?, json: JSONObject ->
            concurrentHandlerHolder.uiScope.launch {
                val payload = json.optJSONObject("payload")
                val currentActivity = currentActivityProvider.get()
                if (property != null && currentActivity != null) {
                    inAppInternal.eventHandler?.handleEvent(currentActivity, property, payload)
                }
            }
        }
    }

    private fun setupDialogWithActions(iamDialog: IamDialog) {
        val saveDisplayedIamAction: OnDialogShownAction = SaveDisplayedIamAction(
            concurrentHandlerHolder,
            displayedIamRepository,
            timestampProvider
        )
        val sendDisplayedIamAction: OnDialogShownAction = SendDisplayedIamAction(
            concurrentHandlerHolder,
            inAppInternal
        )
        iamDialog.setActions(listOf(saveDisplayedIamAction, sendDisplayedIamAction))
    }

}