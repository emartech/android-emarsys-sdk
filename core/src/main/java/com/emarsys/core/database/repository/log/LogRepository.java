package com.emarsys.core.database.repository.log;

import android.content.Context;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;

import java.util.List;
import java.util.Map;

public class LogRepository implements Repository<Map<String, Object>, SqlSpecification> {

    Context context;

    public LogRepository(Context context) {
        this.context = context;
    }

    @Override
    public void add(Map<String, Object> item) {

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
