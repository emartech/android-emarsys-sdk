package com.emarsys.mobileengage.iam.model.requestRepositoryProxy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.emarsys.mobileengage.util.RequestModelHelper;
import com.emarsys.mobileengage.util.RequestPayloadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.coroutines.Continuation;


public class RequestRepositoryProxy implements Repository<RequestModel, SqlSpecification> {

    private final Repository<RequestModel, SqlSpecification> requestRepository;
    private final Repository<DisplayedIam, SqlSpecification> iamRepository;
    private final Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final InAppEventHandlerInternal inAppEventHandlerInternal;
    private final ServiceEndpointProvider eventServiceProvider;
    private final RequestModelHelper requestModelHelper;

    public RequestRepositoryProxy(
            Repository<RequestModel, SqlSpecification> requestRepository,
            Repository<DisplayedIam, SqlSpecification> iamRepository,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider,
            InAppEventHandlerInternal inAppEventHandlerInternal,
            ServiceEndpointProvider eventServiceProvider,
            RequestModelHelper requestModelHelper) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(iamRepository, "IamRepository must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickedRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(inAppEventHandlerInternal, "InAppEventHandlerInternal must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");
        Assert.notNull(requestModelHelper, "RequestModelHelper must not be null!");

        this.requestRepository = requestRepository;
        this.iamRepository = iamRepository;
        this.buttonClickedRepository = buttonClickedRepository;
        this.timestampProvider = timestampProvider;
        this.inAppEventHandlerInternal = inAppEventHandlerInternal;
        this.uuidProvider = uuidProvider;
        this.eventServiceProvider = eventServiceProvider;
        this.requestModelHelper = requestModelHelper;
    }

    @Nullable
    @Override
    public Object update(RequestModel item, @NonNull SqlSpecification specification, @NonNull Continuation<? super Integer> $completion) {
        throw new UnsupportedOperationException("update method is not supported in RequestRepositoryProxy");
    }

    @Nullable
    @Override
    public Object add(RequestModel item, @NonNull Continuation<? super Unit> $completion) {
        Object result = null;
        if (!(item instanceof CompositeRequestModel)) {
            result = requestRepository.add(item, $completion);
        }
        return result;
    }

    @Nullable
    @Override
    public Object remove(SqlSpecification specification, @NonNull Continuation<? super Unit> $completion) {
        return requestRepository.remove(specification, $completion);
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
            if (requestModelHelper.isCustomEvent(requestModel)) {
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
}
