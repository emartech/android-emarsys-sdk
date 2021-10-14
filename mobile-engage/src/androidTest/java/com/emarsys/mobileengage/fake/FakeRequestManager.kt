package com.emarsys.mobileengage.fake

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import kotlinx.coroutines.CoroutineScope
import org.mockito.kotlin.mock

@Mockable
class FakeRequestManager(private val responseType: ResponseType, private val response: ResponseModel) : RequestManager(
    CoreSdkHandler(Handler(Looper.getMainLooper())),
    mock() as Repository<RequestModel, SqlSpecification>,
    mock() as Repository<ShardModel, SqlSpecification>,
    mock(),
    mock(),
    mock() as Registry<RequestModel, CompletionListener?>,
    mock(),
    mock(),
    mock(),
    mock()
) {

    enum class ResponseType {
        SUCCESS,
        FAILURE,
        EXCEPTION
    }

    override fun submitNow(requestModel: RequestModel, coreCompletionHandler: CoreCompletionHandler, scope: CoroutineScope) {
        if (responseType == ResponseType.SUCCESS) {
            coreCompletionHandler.onSuccess("id", response)
        } else if (responseType == ResponseType.FAILURE) {
            coreCompletionHandler.onError("id", response)
        } else {
            coreCompletionHandler.onError("id", Exception("Test exception"))
        }
    }
}