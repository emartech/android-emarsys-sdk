package com.emarsys.core.database.repository.specification;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueryAllTest {

    private QueryAll specification;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        specification = new QueryAll("table");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_tableNameMustNotBeNull() {
        new QueryAll(null);
    }

    @Test
    public void testGetSql() {
        String expected = "SELECT * FROM table;";
        String result = specification.getSql();

        assertEquals(expected, result);
    }

    @Test
    public void testGetArgs() {
        assertNull(specification.getArgs());
    }

}