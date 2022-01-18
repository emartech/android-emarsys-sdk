package com.emarsys.core.worker

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.connection.ConnectionChangeListener
import com.emarsys.core.connection.ConnectionState
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.request.RequestExpiredException
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.specification.FilterByRequestIds
import com.emarsys.core.request.model.specification.QueryLatestRequestModel
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.OfflineQueueSize
import kotlinx.coroutines.runBlocking

@Mockable
class DefaultWorker(
    var requestRepository: Repository<RequestModel, SqlSpecification>,
    var connectionWatchDog: ConnectionWatchDog,
    val uiHandler: Handler,
    var coreCompletionHandler: CoreCompletionHandler,
    var restClient: RestClient,
    val proxyProvider: CompletionHandlerProxyProvider
) : ConnectionChangeListener, Worker {

    final override var isLocked = false
        private set

    init {
        this.connectionWatchDog.registerReceiver(this)
    }

    override fun lock() {
        isLocked = true
    }

    override fun unlock() {
        isLocked = false
    }

    override fun run() {
        if (!isLocked && connectionWatchDog.isConnected && !requestRepository.isEmpty()) {
            lock()
            val model = findFirstNonExpiredModel()
            if (model != null) {
                restClient.execute(
                    model,
                    proxyProvider.provideProxy(this, coreCompletionHandler)
                )
            } else {
                unlock()
            }
        }
    }

    override fun onConnectionChanged(connectionState: ConnectionState?, isConnected: Boolean) {
        if (isConnected) {
            debug(OfflineQueueSize(requestRepository.query(Everything()).size), false)
            run()
        }
    }

    private fun findFirstNonExpiredModel(): RequestModel? {
        while (!requestRepository.isEmpty()) {
            val result = requestRepository.query(QueryLatestRequestModel())
            if (!result.isEmpty()) {
                val model = result[0]
                if (isExpired(model)) {
                    handleExpiration(model)
                } else {
                    return model
                }
            } else {
                break
            }
        }
        return null
    }

    private fun isExpired(model: RequestModel): Boolean {
        val now = System.currentTimeMillis()
        return now - model.timestamp > model.ttl
    }

    private fun handleExpiration(expiredModel: RequestModel) {
        val ids = arrayOf(expiredModel.id)
        runBlocking {
            requestRepository.remove(FilterByRequestIds(ids))
        }
        uiHandler.post {
            coreCompletionHandler.onError(
                expiredModel.id,
                RequestExpiredException("Request expired", expiredModel.url.path)
            )
        }
    }
}