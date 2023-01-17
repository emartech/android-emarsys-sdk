package com.emarsys.mobileengage.iam.model.requestRepositoryProxy

import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.specification.FilterByUrlPattern
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.mobileengage.util.RequestPayloadUtils.createCompositeRequestModelPayload

class RequestRepositoryProxy(
    private val requestRepository: Repository<RequestModel, SqlSpecification>,
    private val iamRepository: Repository<DisplayedIam, SqlSpecification>,
    private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UUIDProvider,
    private val inAppEventHandlerInternal: InAppEventHandlerInternal,
    private val eventServiceProvider: ServiceEndpointProvider,
    private val requestModelHelper: RequestModelHelper
) : Repository<RequestModel, SqlSpecification> {

    override fun add(item: RequestModel) {
        if (item !is CompositeRequestModel) {
            requestRepository.add(item)
        }
    }

    override fun remove(specification: SqlSpecification) {
        requestRepository.remove(specification)
    }

    override fun isEmpty(): Boolean {
        return requestRepository.isEmpty()
    }

    override fun query(specification: SqlSpecification): List<RequestModel> {
        val result = requestRepository.query(specification).toMutableList()
        val customEventsInResult = collectCustomEvents(result)
        if (customEventsInResult.isNotEmpty()) {
            val customEvents =
                requestRepository.query(FilterByUrlPattern(eventServiceProvider.provideEndpointHost() + "%"))
            if (customEvents.isNotEmpty()) {
                val composite: RequestModel = createCompositeCustomEvent(customEvents)
                val firstCustomEvent = customEventsInResult[0]
                val firstCustomEventIndex = result.indexOf(firstCustomEvent)
                result.add(firstCustomEventIndex, composite)
                result.removeAll(customEventsInResult)
            }
        }
        return result
    }

    private fun collectCustomEvents(models: List<RequestModel>): List<RequestModel> {
        val result: MutableList<RequestModel> = ArrayList()
        for (requestModel in models) {
            if (requestModelHelper.isCustomEvent(requestModel)) {
                result.add(requestModel)
            }
        }
        return result
    }

    private fun createCompositeCustomEvent(models: List<RequestModel>): CompositeRequestModel {
        val first = models[0]
        val payload = createCompositePayload(models)
        val requestIds = collectRequestIds(models)
        return CompositeRequestModel.Builder(timestampProvider, uuidProvider)
            .url(first.url.toString())
            .method(first.method)
            .payload(payload)
            .headers(first.headers)
            .ttl(Long.MAX_VALUE)
            .originalRequestIds(requestIds).build()
    }

    private fun createCompositePayload(models: List<RequestModel>): Map<String, Any?> {
        val events: MutableList<Any> = ArrayList()
        for (model in models) {
            val individualEvents = model.payload!!["events"]
            if (individualEvents != null && individualEvents is List<*>) {
                events.addAll(individualEvents as Collection<Any>)
            }
        }
        return createCompositeRequestModelPayload(
            events,
            iamRepository.query(Everything()),
            buttonClickedRepository.query(Everything()),
            inAppEventHandlerInternal.isPaused
        )
    }

    private fun collectRequestIds(models: List<RequestModel>): Array<String> {
        return models.map { it.id }.toTypedArray()
    }

    override fun update(item: RequestModel, specification: SqlSpecification): Int {
        throw UnsupportedOperationException("update method is not supported in RequestRepositoryProxy")
    }
}