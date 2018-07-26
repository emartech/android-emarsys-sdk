package com.emarsys.mobileengage.log;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.handler.Handler;
import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogRepositoryProxy implements Repository<Map<String, Object>, SqlSpecification> {

    private final Repository<Map<String, Object>, SqlSpecification> logRepository;

    private final List<Handler<Map<String, Object>, Map<String, Object>>> handlers;

    public LogRepositoryProxy(
            Repository<Map<String, Object>, SqlSpecification> logRepository,
            List<Handler<Map<String, Object>, Map<String, Object>>> handlers) {
        Assert.notNull(logRepository, "LogRepository must not be null!");
        Assert.notNull(handlers, "Handlers must not be null!");
        Assert.elementsNotNull(handlers, "Handler elements must not be null!");
        this.logRepository = logRepository;
        this.handlers = handlers;
    }

    @Override
    public void add(Map<String, Object> item) {
        List<Map<String, Object>> handledMaps = new ArrayList<>();

        for (Handler<Map<String, Object>, Map<String, Object>> handler : handlers) {
            Map<String, Object> handledMap = handler.handle(item);
            if (handledMap != null) {
                handledMaps.add(handledMap);
            }
        }

        if (!handledMaps.isEmpty()) {
            Map<String, Object> mergedResult = new HashMap<>();
            for (Map<String, Object> m : handledMaps) {
                mergedResult.putAll(m);
            }
            logRepository.add(mergedResult);
        }
    }

    @Override
    public void remove(SqlSpecification specification) {

    }

    @Override
    public List<Map<String, Object>> query(SqlSpecification specification) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
