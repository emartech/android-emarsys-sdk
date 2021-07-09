package com.emarsys.mobileengage.iam.dialog

import android.os.Bundle
import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import java.util.concurrent.CountDownLatch

@Mockable
class IamDialogProvider(private val uiHandler: Handler, private val timestampProvider: TimestampProvider) {
    fun provideDialog(campaignId: String, sid: String?, url: String?, requestId: String?): IamDialog {
        var dialog: IamDialog? = null
        val latch = CountDownLatch(1)
        uiHandler.post {
            dialog = IamDialog(uiHandler, timestampProvider)
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