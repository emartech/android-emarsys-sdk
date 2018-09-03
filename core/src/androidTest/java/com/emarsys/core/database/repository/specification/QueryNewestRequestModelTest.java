package com.emarsys.core.database.repository.specification;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.request.model.specification.QueryNewestRequestModel;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class QueryNewestRequestModelTest {

    private QueryNewestRequestModel specification;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        specification = new QueryNewestRequestModel();
    }

    @Test
    public void testGetSql() {
        String expected = String.format("SELECT * FROM %s ORDER BY ROWID ASC LIMIT 1;", DatabaseContract.REQUEST_TABLE_NAME);
        String result = specification.getSql();

        assertEquals(expected, result);
    }

    @Test
    public void testGetArgs() {
        assertNull(specification.getArgs());
    }

}