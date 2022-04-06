package com.emarsys.mobileengage.iam.dialog.action

import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam

class SaveDisplayedIamAction(
    var concurrentHandlerHolder: ConcurrentHandlerHolder,
    var repository: Repository<DisplayedIam, SqlSpecification>,
    var timestampProvider: TimestampProvider
) : OnDialogShownAction {

    override fun execute(campaignId: String, sid: String?, url: String?) {
        concurrentHandlerHolder.coreHandler.post {
            val iam = DisplayedIam(campaignId, timestampProvider.provideTimestamp())
            repository.add(iam)
        }
    }
}