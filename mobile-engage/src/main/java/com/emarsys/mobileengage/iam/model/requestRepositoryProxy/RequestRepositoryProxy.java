package com.emarsys.mobileengage.iam.model.requestRepositoryProxy;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.specification.Everything;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.FilterByUrlPattern;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.util.RequestModelUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RequestRepositoryProxy implements Repository<RequestModel, SqlSpecification> {

    private final Repository<RequestModel, SqlSpecification> requestRepository;
    private final Repository<DisplayedIam, SqlSpecification> iamRepository;
    private final Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final InAppEventHandlerInternal inAppEventHandlerInternal;
    private final ServiceEndpointProvider eventServiceProvider;

    public RequestRepositoryProxy(
            Repository<RequestModel, SqlSpecification> requestRepository,
            Repository<DisplayedIam, SqlSpecification> iamRepository,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider,
            InAppEventHandlerInternal inAppEventHandlerInternal, ServiceEndpointProvider eventServiceProvider) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(iamRepository, "IamRepository must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickedRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(inAppEventHandlerInternal, "InAppEventHandlerInternal must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");

        this.requestRepository = requestRepository;
        this.iamRepository = iamRepository;
        this.buttonClickedRepository = buttonClickedRepository;
        this.timestampProvider = timestampProvider;
        this.inAppEventHandlerInternal = inAppEventHandlerInternal;
        this.uuidProvider = uuidProvider;
        this.eventServiceProvider = eventServiceProvider;
    }

    @Override
    public void add(RequestModel item) {
        if (!(item instanceof CompositeRequestModel)) {
            requestRepository.add(item);
        }
    }

    @Override
    public void remove(SqlSpecification specification) {
        requestRepository.remove(specification);
    }

    @Override
    public boolean isEmpty() {
        return requestRepository.isEmpty();
    }

    @Override
    public List<RequestModel> query(SqlSpecification specification) {
        List<RequestModel> result = requestRepository.query(specification);
        List<RequestModel> customEventsInResult = collectCustomEvents(result);

        if (!customEventsInResult.isEmpty()) {
            List<RequestModel> customEvents = requestRepository.query(new FilterByUrlPattern(eventServiceProvider.provideEndpointHost() + "%"));
            RequestModel composite = createCompositeCustomEvent(customEvents);

            RequestModel firstCustomEvent = customEventsInResult.get(0);
            int firstCustomEventIndex = result.indexOf(firstCustomEvent);
            result.add(firstCustomEventIndex, composite);

            result.removeAll(customEventsInResult);
        }
        return result;
    }

    private List<RequestModel> collectCustomEvents(List<RequestModel> models) {
        List<RequestModel> result = new ArrayList<>();
        for (RequestModel requestModel : models) {
            if (RequestModelUtils.isCustomEvent_V3(requestModel, eventServiceProvider)) {
                result.add(requestModel);
            }
        }
        return result;
    }

    private CompositeRequestModel createCompositeCustomEvent(List<RequestModel> models) {
        RequestModel first = models.get(0);
        Map<String, Object> payload = createCompositePayload(models);
        String[] requestIds = collectRequestIds(models);
        return new CompositeRequestModel.Builder(timestampProvider, uuidProvider)
                .url(first.getUrl().toString())
                .method(first.getMethod())
                .payload(payload)
                .headers(first.getHeaders())
                .ttl(Long.MAX_VALUE)
                .originalRequestIds(requestIds).build();
    }

    private Map<String, Object> createCompositePayload(List<RequestModel> models) {
        List<Object> events = new ArrayList<>();

        for (RequestModel model : models) {
            Object individualEvents = model.getPayload().get("events");
            if (individualEvents != null && individualEvents instanceof List) {
                events.addAll((List) individualEvents);
            }
        }

        return RequestPayloadUtils.createCompositeRequestModelPayload(
                events,
                iamRepository.query(new Everything()),
                buttonClickedRepository.query(new Everything()),
                inAppEventHandlerInternal.isPaused()
        );
    }

    private String[] collectRequestIds(List<RequestModel> models) {
        int size = models.size();
        String[] result = new String[size];

        for (int i = 0; i < size; ++i) {
            result[i] = models.get(i).getId();
        }

        return result;
    }

    @Override
    public int update(RequestModel item, @NotNull SqlSpecification specification) {
        throw new UnsupportedOperationException("update method is not supported in RequestRepositoryProxy");
    }
}
