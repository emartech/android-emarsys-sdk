package com.emarsys.mobileengage.iam.dialog

import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider
import java.util.concurrent.CountDownLatch

@Mockable
class IamDialogProvider(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val timestampProvider: TimestampProvider,
    private val inAppInternal: InAppInternal,
    private val displayedIamRepository: Repository<DisplayedIam, SqlSpecification>,
    private val webViewProvider: IamWebViewProvider
) {
    fun provideDialog(
        campaignId: String,
        sid: String?,
        url: String?,
        requestId: String?
    ): IamDialog {
        lateinit var dialog: IamDialog
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.postOnMain {
            dialog = IamDialog(timestampProvider, webViewProvider)
            val bundle = Bundle()
            bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
            bundle.putString(IamDialog.SID, sid)
            bundle.putString(IamDialog.URL, url)
            bundle.putString(IamDialog.REQUEST_ID, requestId)
            dialog.arguments = bundle
            latch.countDown()
        }
        latch.await()
        dialog.setActions(dialogActions())
        return dialog
    }

    private fun dialogActions(): List<OnDialogShownAction> {
        val saveDisplayedIamAction: OnDialogShownAction = SaveDisplayedIamAction(
            concurrentHandlerHolder,
            displayedIamRepository,
            timestampProvider
        )
        val sendDisplayedIamAction: OnDialogShownAction = SendDisplayedIamAction(
            concurrentHandlerHolder,
            inAppInternal
        )
        return listOf(saveDisplayedIamAction, sendDisplayedIamAction)
    }
}