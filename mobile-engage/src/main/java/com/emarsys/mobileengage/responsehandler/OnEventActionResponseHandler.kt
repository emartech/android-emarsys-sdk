package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils.toMutableList
import com.emarsys.mobileengage.notification.ActionCommandFactory
import org.json.JSONException
import org.json.JSONObject

class OnEventActionResponseHandler(private val actionCommandFactory: ActionCommandFactory) : AbstractResponseHandler() {

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
        val responseBody = responseModel.parsedBody
        val onEventAction = responseBody.getJSONObject("onEventAction")
        onEventAction.getJSONArray("actions").toMutableList().map {
            actionCommandFactory.createActionCommand(it)
        }.forEach {
            it?.run()
        }
    }
}