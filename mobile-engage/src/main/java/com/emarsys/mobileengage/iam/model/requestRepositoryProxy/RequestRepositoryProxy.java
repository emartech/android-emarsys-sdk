package com.emarsys.mobileengage.iam.model.requestRepositoryProxy;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.specification.QueryAll;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.FilterByUrlPattern;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.endpoint.Endpoint;
import com.emarsys.mobileengage.iam.DoNotDisturbProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamContract;
import com.emarsys.mobileengage.util.RequestModelUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RequestRepositoryProxy implements Repository<RequestModel, SqlSpecification> {

    private static final String CUSTOM_EVENT_URL_PATTERN = Endpoint.ME_BASE_V3 + "_%/events";

    private final DeviceInfo deviceInfo;
    private final Repository<RequestModel, SqlSpecification> requestRepository;
    private final Repository<DisplayedIam, SqlSpecification> iamRepository;
    private final Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private final TimestampProvider timestampProvider;
    private final DoNotDisturbProvider doNotDisturbProvider;

    public RequestRepositoryProxy(
            DeviceInfo deviceInfo,
            Repository<RequestModel, SqlSpecification> requestRepository,
            Repository<DisplayedIam, SqlSpecification> iamRepository,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            TimestampProvider timestampProvider,
            DoNotDisturbProvider doNotDisturbProvider) {
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(iamRepository, "IamRepository must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickedRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(doNotDisturbProvider, "DoNotDisturbProvider must not be null!");
        this.deviceInfo = deviceInfo;
        this.requestRepository = requestRepository;
        this.iamRepository = iamRepository;
        this.buttonClickedRepository = buttonClickedRepository;
        this.timestampProvider = timestampProvider;
        this.doNotDisturbProvider = doNotDisturbProvider;
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
            List<RequestModel> customEvents = requestRepository.query(new FilterByUrlPattern(CUSTOM_EVENT_URL_PATTERN));
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
            if (RequestModelUtils.isCustomEvent_V3(requestModel)) {
                result.add(requestModel);
            }
        }
        return result;
    }

    private CompositeRequestModel createCompositeCustomEvent(List<RequestModel> models) {
        RequestModel first = models.get(0);
        Map<String, Object> payload = createCompositePayload(models);
        String[] requestIds = collectRequestIds(models);

        return new CompositeRequestModel(
                first.getUrl().toString(),
                first.getMethod(),
                payload,
                first.getHeaders(),
                timestampProvider.provideTimestamp(),
                Long.MAX_VALUE,
                requestIds
        );
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
                iamRepository.query(new QueryAll(DisplayedIamContract.TABLE_NAME)),
                buttonClickedRepository.query(new QueryAll(ButtonClickedContract.TABLE_NAME)),
                deviceInfo,
                doNotDisturbProvider.isPaused()
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
