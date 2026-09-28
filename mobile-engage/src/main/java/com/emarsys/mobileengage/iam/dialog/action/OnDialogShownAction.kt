package com.emarsys.mobileengage.iam.dialog.action

fun interface OnDialogShownAction {
    fun execute(campaignId: String, sid: String?, url: String?)
}
