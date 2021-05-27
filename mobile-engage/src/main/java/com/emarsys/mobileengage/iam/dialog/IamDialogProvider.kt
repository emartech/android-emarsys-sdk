package com.emarsys.mobileengage.iam.dialog

import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.iam.dialog.IamDialog.Companion.create
import java.util.concurrent.CountDownLatch

@Mockable
class IamDialogProvider(private val uiHandler: Handler) {
    fun provideDialog(campaignId: String, sid: String?, url: String?, requestId: String?): IamDialog {
        var dialog: IamDialog? = null
        val latch = CountDownLatch(1)
        uiHandler.post {
            dialog = create(campaignId, sid, url, requestId)
            latch.countDown()
        }
        latch.await()
        return dialog!!
    }
}