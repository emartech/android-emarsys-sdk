package com.emarsys.mobileengage.responsehandler

import android.os.Handler
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils.toMutableList
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.notification.ActionCommandFactory
import org.json.JSONException
import org.json.JSONObject

class OnEventActionResponseHandler(private val actionCommandFactory: ActionCommandFactory,
                                   private val repository: Repository<DisplayedIam, SqlSpecification>,
                                   private val eventServiceInternal: EventServiceInternal,
                                   private val timestampProvider: TimestampProvider,
                                   private val coreSdkHandler: Handler) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        var shouldHandle = false

        try {
            val onEventAction: JSONObject? = responseModel.parsedBody?.getJSONObject("onEventAction")
            shouldHandle = onEventAction?.has("actions") ?: false
        } catch (ignored: JSONException) {
        }

        return shouldHandle
    }

    override fun handleResponse(responseModel: ResponseModel) {
        try {
            val responseBody = responseModel.parsedBody
            val onEventAction = responseBody.getJSONObject("onEventAction")
            val campaignId = onEventAction.getString("campaignId")
            onEventAction.getJSONArray("actions").toMutableList().map {
                actionCommandFactory.createActionCommand(it)
            }.forEach {
                it?.run()
            }

            SaveDisplayedIamAction(coreSdkHandler, repository, timestampProvider).execute(campaignId, null, null)
            SendDisplayedIamAction(coreSdkHandler, eventServiceInternal).execute(campaignId, null, null)

        } catch (exception: JSONException) {
            Logger.error(CrashLog(exception))
        }
    }
}