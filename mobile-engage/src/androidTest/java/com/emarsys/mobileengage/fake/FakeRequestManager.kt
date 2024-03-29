package com.emarsys.mobileengage.fake

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import org.mockito.kotlin.mock


class FakeRequestManager(
    private val responseType: ResponseType,
    private val response: ResponseModel
) : RequestManager(
    ConcurrentHandlerHolderFactory.create(),
    mock() as Repository<RequestModel, SqlSpecification>,
    mock() as Repository<ShardModel, SqlSpecification>,
    mock(),
    mock(),
    mock() as Registry<RequestModel, CompletionListener?>,
    mock(),
    mock(),
    mock()
) {

    enum class ResponseType {
        SUCCESS,
        FAILURE,
        EXCEPTION
    }

    override fun submitNow(
        requestModel: RequestModel,
        completionHandler: CoreCompletionHandler
    ) {
        when (responseType) {
            ResponseType.SUCCESS -> {
                completionHandler.onSuccess("id", response)
            }
            ResponseType.FAILURE -> {
                completionHandler.onError("id", response)
            }
            else -> {
                completionHandler.onError("id", Exception("Test exception"))
            }
        }
    }
}