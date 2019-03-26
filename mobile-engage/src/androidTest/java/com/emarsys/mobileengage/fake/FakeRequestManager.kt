package com.emarsys.mobileengage.fake

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.worker.Worker
import org.mockito.Mockito.mock

@Suppress("UNCHECKED_CAST")
class FakeRequestManager(private val responseType: ResponseType, private val response: ResponseModel) : RequestManager(
        Handler(Looper.getMainLooper()),
        mock(Repository::class.java) as Repository<RequestModel, SqlSpecification>,
        mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>,
        mock(Worker::class.java),
        mock(RestClient::class.java),
        mock(Registry::class.java) as Registry<RequestModel, CompletionListener>,
        mock(CoreCompletionHandler::class.java)) {

    enum class ResponseType {
        SUCCESS,
        FAILURE,
        EXCEPTION
    }

    override fun submitNow(requestModel: RequestModel, coreCompletionHandler: CoreCompletionHandler) {
        if (responseType == ResponseType.SUCCESS) {
            coreCompletionHandler.onSuccess("id", response)
        } else if (responseType == ResponseType.FAILURE) {
            coreCompletionHandler.onError("id", response)
        } else {
            coreCompletionHandler.onError("id", Exception("Test exception"))
        }
    }
}