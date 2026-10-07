package com.emarsys.mobileengage.fake

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.handler.SdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.response.ResponseModel
import org.mockito.kotlin.mock

class FakeRestClient private constructor(
    private val mode: Mode,
    private val responses: MutableList<ResponseModel>,
    private val exceptions: MutableList<Exception>,
    private var handler: SdkHandler?
) : RestClient(
    mock<ConnectionProvider>(),
    mock<TimestampProvider>(),
    mock<ResponseHandlersProcessor>(),
    emptyList(),
    ConcurrentHandlerHolderFactory.create()
) {
    enum class Mode { SUCCESS, ERROR_RESPONSE_MODEL, ERROR_EXCEPTION }

    constructor(returnValue: ResponseModel, mode: Mode) :
        this(mode, mutableListOf(returnValue), mutableListOf(), null)

    constructor(responses: List<ResponseModel>, mode: Mode) :
        this(mode, responses.toMutableList(), mutableListOf(), null)

    constructor(exception: Exception) :
        this(Mode.ERROR_EXCEPTION, mutableListOf(), mutableListOf(exception), null)

    constructor(exception: Exception, handler: SdkHandler) :
        this(Mode.ERROR_EXCEPTION, mutableListOf(), mutableListOf(exception), handler)

    constructor(exceptions: List<Exception>) :
        this(Mode.ERROR_EXCEPTION, mutableListOf(), exceptions.toMutableList(), null)

    constructor(returnValue: ResponseModel, mode: Mode, handler: SdkHandler) :
        this(mode, mutableListOf(returnValue), mutableListOf(), handler)

    override fun execute(model: RequestModel, completionHandler: CoreCompletionHandler) {
        val h = handler ?: SdkHandler(Handler(Looper.getMainLooper()))
        h.postDelayed({
            when (mode) {
                Mode.SUCCESS -> completionHandler.onSuccess(model.id, getCurrentItem(responses))
                Mode.ERROR_RESPONSE_MODEL -> completionHandler.onError(model.id, getCurrentItem(responses))
                Mode.ERROR_EXCEPTION -> completionHandler.onError(model.id, getCurrentItem(exceptions))
            }
        }, 100)
    }

    private fun <T> getCurrentItem(list: MutableList<T>): T {
        val result = list[0]
        if (list.size > 1) list.removeAt(0)
        return result
    }
}
