package com.emarsys.fake

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import org.mockito.kotlin.mock

class FakeRestClient(
    private var mode: Mode = Mode.SUCCESS,
    private var responses: List<ResponseModel> = listOf(),
    private var exceptions: List<Exception> = listOf(),
    response: ResponseModel? = null,
    exception: Exception? = null
) : RestClient(mock(), mock(), mock(), mock(), ConcurrentHandlerHolderFactory.create()) {
    init {
        if (exception != null) {
            exceptions = mutableListOf(exception)
            mode = Mode.ERROR_EXCEPTION
        }
        if (response != null) {
            responses = mutableListOf(response)
        }
    }

    enum class Mode {
        SUCCESS, ERROR_RESPONSE_MODEL, ERROR_EXCEPTION
    }

    override fun execute(model: RequestModel, completionHandler: CoreCompletionHandler) {
        Handler(Looper.getMainLooper()).postDelayed({
            when (mode) {
                Mode.SUCCESS -> {
                    completionHandler.onSuccess(model.id, getCurrentItem(responses))
                }
                Mode.ERROR_RESPONSE_MODEL -> {
                    completionHandler.onError(model.id, getCurrentItem(responses))
                }
                Mode.ERROR_EXCEPTION -> {
                    completionHandler.onError(model.id, getCurrentItem(exceptions))
                }
            }
        }, 100)
    }

    private fun <T> getCurrentItem(sourceList: List<T>): T {
        val mutableList = sourceList.toMutableList()
        val result = mutableList[0]
        if (mutableList.size > 1) {
            mutableList.removeAt(0)
        }
        return result
    }
}
