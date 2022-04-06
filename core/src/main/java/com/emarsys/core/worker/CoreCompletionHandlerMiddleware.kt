package com.emarsys.core.worker

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.collectRequestIds
import com.emarsys.core.request.model.specification.FilterByRequestIds
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.RequestModelUtils
import kotlin.math.min

@Mockable
class CoreCompletionHandlerMiddleware(
    private var worker: Worker?,
    private val requestRepository: Repository<RequestModel, SqlSpecification>,
    var concurrentHandlerHolder: ConcurrentHandlerHolder,
    val coreCompletionHandler: CoreCompletionHandler?
) : CoreCompletionHandler {

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        concurrentHandlerHolder.post {
            removeRequestModel(responseModel)
            worker?.unlock()
            worker?.run()
            handleSuccess(responseModel)
        }
    }

    override fun onError(id: String, responseModel: ResponseModel) {
        concurrentHandlerHolder.post {
            if (isNonRetriableError(responseModel.statusCode)) {
                removeRequestModel(responseModel)
                handleError(responseModel)
                worker?.unlock()
                worker?.run()
            } else {
                worker?.unlock()
            }
        }
    }

    private fun removeRequestModel(responseModel: ResponseModel) {
        val ids = responseModel.requestModel.collectRequestIds()
        val maximumRequestCountInTransaction = 50
        val noOfIterations =
            if (ids.size % maximumRequestCountInTransaction == 0) ids.size / maximumRequestCountInTransaction else ids.size / maximumRequestCountInTransaction + 1
        for (i in 0 until noOfIterations) {
            val noOfElements = min(ids.size, (i + 1) * maximumRequestCountInTransaction)
            requestRepository.remove(
                FilterByRequestIds(
                    ids.copyOfRange(i * maximumRequestCountInTransaction, noOfElements)
                )
            )
        }
    }

    override fun onError(id: String, cause: Exception) {
        concurrentHandlerHolder.post {
            worker?.unlock()
            concurrentHandlerHolder.postOnMain {
                coreCompletionHandler?.onError(
                    id,
                    cause
                )
            }
        }
    }

    private fun isNonRetriableError(statusCode: Int): Boolean {
        return if (statusCode == 408 || statusCode == 429) {
            false
        } else {
            400 <= statusCode && statusCode < 500
        }
    }

    private fun handleSuccess(responseModel: ResponseModel) {
        for (id in RequestModelUtils.extractIdsFromCompositeRequestModel(responseModel.requestModel)) {
            concurrentHandlerHolder.postOnMain {
                coreCompletionHandler?.onSuccess(
                    id,
                    responseModel
                )
            }
        }
    }

    private fun handleError(responseModel: ResponseModel) {
        for (id in RequestModelUtils.extractIdsFromCompositeRequestModel(responseModel.requestModel)) {
            concurrentHandlerHolder.postOnMain {
                coreCompletionHandler?.onError(
                    id,
                    responseModel
                )
            }
        }
    }
}