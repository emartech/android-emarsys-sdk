package com.emarsys.core.database.repository.specification;

import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.request.model.specification.FilterByRequestId;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FilterByRequestIdTest {

    private RequestModelRepository repository;
    private RequestModel requestModel1;
    private RequestModel requestModel2;
    private RequestModel requestModel3;
    private RequestModel requestModel4;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();
        TimestampProvider timestampProvider = new TimestampProvider();
        UUIDProvider uuidProvider = new UUIDProvider();

        requestModel1 = new RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/1").build();
        requestModel2 = new RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/2").build();
        requestModel3 = new RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/3").build();
        requestModel4 = new RequestModel.Builder(timestampProvider, uuidProvider).url("https://emarsys.com/4").build();

        CoreDbHelper coreDbHelper = new CoreDbHelper(InstrumentationRegistry.getTargetContext(), new HashMap<TriggerKey, List<Runnable>>());
        repository = new RequestModelRepository(coreDbHelper);

        repository.add(requestModel1);
        repository.add(requestModel2);
        repository.add(requestModel3);
        repository.add(requestModel4);
    }

    @Test
    public void testExecution_withRequestModel() {
        List<RequestModel> expected = Arrays.asList(requestModel1, requestModel3, requestModel4);

        repository.remove(new FilterByRequestId(requestModel2));

        List<RequestModel> actual = repository.query(new QueryAll(DatabaseContract.REQUEST_TABLE_NAME));

        assertEquals(expected, actual);
    }

    @Test
    public void testExecution_withCompositeRequestModel() {
        List<RequestModel> expected = Arrays.asList(requestModel2);

        String[] originalRequestIds = new String[]{
                requestModel1.getId(),
                requestModel3.getId(),
                requestModel4.getId()};

        RequestModel composite = new CompositeRequestModel(
                "https://emarsys.com",
                RequestMethod.POST,
                null,
                new HashMap<String, String>(),
                System.currentTimeMillis(),
                10_000,
                originalRequestIds);

        repository.remove(new FilterByRequestId(composite));

        List<RequestModel> actual = repository.query(new QueryAll(DatabaseContract.REQUEST_TABLE_NAME));

        assertEquals(expected, actual);
    }

}