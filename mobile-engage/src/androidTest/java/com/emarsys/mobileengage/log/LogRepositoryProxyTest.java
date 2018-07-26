package com.emarsys.mobileengage.log;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.handler.Handler;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class LogRepositoryProxyTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private LogRepositoryProxy proxyRepository;
    private Map<String, Object> map1;
    private Map<String, Object> map2;
    private Map<String, Object> map3;

    @Before
    public void init() {
        logRepository = mock(Repository.class);
        proxyRepository = new LogRepositoryProxy(logRepository, Collections.<Handler<Map<String, Object>, Map<String, Object>>>emptyList());

        map1 = new HashMap<>();
        map1.put("a", "b");
        map1.put("c", false);

        map2 = new HashMap<>();
        map2.put("d", 567889);
        map2.put("e", 3.14);

        map3 = new HashMap<>();
        map3.put("f", true);
        map3.put("g", "something");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepository_mustNotBeNull() {
        new LogRepositoryProxy(null, new ArrayList<Handler<Map<String, Object>, Map<String, Object>>>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlers_mustNotBeNull() {
        new LogRepositoryProxy(logRepository, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlerElements_mustNotBeNull() {
        new LogRepositoryProxy(logRepository, Arrays.<Handler<Map<String, Object>, Map<String, Object>>>asList(mock(Handler.class), null, mock(Handler.class)));
    }

    @Test
    public void testAdd_withNoHandlers() {
        proxyRepository.add(map1);

        verifyZeroInteractions(logRepository);
    }

    @Test
    public void testAdd_invokeHandlers() {
        List<Handler<Map<String, Object>, Map<String, Object>>> handlers = createHandlers();
        LogRepositoryProxy logProxyRepository = new LogRepositoryProxy(logRepository, handlers);

        logProxyRepository.add(map1);


        for (Handler<Map<String, Object>, Map<String, Object>> handler : handlers) {
            verify(handler).handle(map1);
        }
    }

    @Test
    public void testAdd_returnsModifiedElement() {
        Handler<Map<String, Object>, Map<String, Object>> handler = mock(Handler.class);
        when(handler.handle(any(Map.class))).thenReturn(map2);

        proxyRepository = new LogRepositoryProxy(logRepository, Arrays.asList(handler));

        proxyRepository.add(map1);

        verify(logRepository).add(map2);
    }

    @Test
    public void testAdd_returnsModifiedElement_andIgnoresNullValuesReturnedByHandlers() {
        Handler<Map<String, Object>, Map<String, Object>> handler1 = mock(Handler.class);
        when(handler1.handle(any(Map.class))).thenReturn(map2);
        Handler<Map<String, Object>, Map<String, Object>> handler2 = mock(Handler.class);
        Handler<Map<String, Object>, Map<String, Object>> handler3 = mock(Handler.class);

        proxyRepository = new LogRepositoryProxy(logRepository, Arrays.asList(handler1, handler2, handler3));

        proxyRepository.add(map1);

        verify(logRepository).add(map2);
    }

    @Test
    public void testAdd_returnsMergedElement_whenMultipleHandlersReturnValidValue() {
        Handler<Map<String, Object>, Map<String, Object>> handler1 = new Handler<Map<String, Object>, Map<String, Object>>() {
            @Override
            public Map handle(Map item) {
                Map m = new HashMap();
                m.putAll(item);
                m.put("111", "___");
                return m;
            }
        };
        Handler<Map<String, Object>, Map<String, Object>> handler2 = new Handler<Map<String, Object>, Map<String, Object>>() {
            @Override
            public Map handle(Map item) {
                item.put("key", true);
                return item;
            }
        };
        Handler<Map<String, Object>, Map<String, Object>> handler3 = mock(Handler.class);

        Map<String, Object> expectedMergedMap = new HashMap<>();
        expectedMergedMap.put("a", "b");
        expectedMergedMap.put("c", false);
        expectedMergedMap.put("111", "___");
        expectedMergedMap.put("key", true);

        proxyRepository = new LogRepositoryProxy(logRepository, Arrays.asList(handler1, handler2, handler3));

        proxyRepository.add(map1);

        verify(logRepository).add(expectedMergedMap);
    }

    @Test
    public void testAdd_returnsMergedElement_whenMultipleHandlersReturnIndependentMaps() {
        Handler<Map<String, Object>, Map<String, Object>> handler1 = mock(Handler.class);
        when(handler1.handle(any(Map.class))).thenReturn(map1);
        Handler<Map<String, Object>, Map<String, Object>> handler2 = mock(Handler.class);
        when(handler2.handle(any(Map.class))).thenReturn(map2);
        Handler<Map<String, Object>, Map<String, Object>> handler3 = mock(Handler.class);
        when(handler3.handle(any(Map.class))).thenReturn(map3);

        Map<String, Object> expectedMergedMap = new HashMap<>();
        expectedMergedMap.putAll(map1);
        expectedMergedMap.putAll(map2);
        expectedMergedMap.putAll(map3);

        proxyRepository = new LogRepositoryProxy(logRepository, Arrays.asList(handler1, handler2, handler3));

        proxyRepository.add(new HashMap<String, Object>());

        verify(logRepository).add(expectedMergedMap);
    }

    private List<Handler<Map<String, Object>, Map<String, Object>>> createHandlers() {
        List<Handler<Map<String, Object>, Map<String, Object>>> result = new ArrayList<>();

        for (int i = 0; i < 5; ++i) {
            result.add(mock(Handler.class));
        }

        return result;
    }


}