package com.emarsys.mobileengage.iam.dialog

import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

@Mockable
class IamDialogProvider(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val timestampProvider: TimestampProvider
) {
    fun provideDialog(
        campaignId: String,
        sid: String?,
        url: String?,
        requestId: String?
    ): IamDialog {
        var dialog: IamDialog? = null
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.uiScope.launch {
            dialog = IamDialog(concurrentHandlerHolder, timestampProvider)
            val bundle = Bundle()
            bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
            bundle.putString(IamDialog.SID, sid)
            bundle.putString(IamDialog.URL, url)
            bundle.putString(IamDialog.REQUEST_ID, requestId)
            dialog!!.arguments = bundle
            latch.countDown()
        }
        latch.await()
        return dialog!!
    }
}