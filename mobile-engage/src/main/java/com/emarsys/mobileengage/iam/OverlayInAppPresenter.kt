package com.emarsys.mobileengage.iam

import com.emarsys.core.Mockable
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.fragmentManager
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener

@Mockable
class OverlayInAppPresenter(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val dialogProvider: IamDialogProvider,
    private val timestampProvider: TimestampProvider,
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog
) {
    private var showingInProgress = false

    fun present(
        campaignId: String, sid: String?, url: String?, requestId: String?, startTimestamp: Long,
        html: String, messageLoadedListener: MessageLoadedListener?
    ) {
        val shownDialog =
            currentActivityWatchdog.activity().fragmentManager()?.findFragmentByTag(IamDialog.TAG)
        if (shownDialog == null && !showingInProgress) {
            showingInProgress = true
            concurrentHandlerHolder.postOnMain {
                try {
                    val iamDialog =
                        dialogProvider.provideDialog(campaignId, sid, url, requestId)
                    val activity = currentActivityWatchdog.activity()
                    iamDialog.loadInApp(
                        html, InAppMetaData(campaignId, sid, url),
                        {
                            activity.fragmentManager()?.let {
                                if (it.findFragmentByTag(IamDialog.TAG) == null) {
                                    val endTimestamp = timestampProvider.provideTimestamp()
                                    iamDialog.setInAppLoadingTime(
                                        InAppLoadingTime(
                                            startTimestamp,
                                            endTimestamp
                                        )
                                    )
                                    if (!it.isStateSaved) {
                                        iamDialog.showNow(it, IamDialog.TAG)
                                    }
                                }
                            }
                            concurrentHandlerHolder.coreHandler.post {
                                messageLoadedListener?.onMessageLoaded()
                                showingInProgress = false
                            }
                        },
                        activity
                    )
                } catch (e: Exception) {
                    concurrentHandlerHolder.coreHandler.post {
                        Logger.error(CrashLog(e))
                        messageLoadedListener?.onMessageLoaded()
                        showingInProgress = false
                    }
                }
            }
        } else {
            messageLoadedListener?.onMessageLoaded()
        }
    }
}