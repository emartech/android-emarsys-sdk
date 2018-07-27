package com.emarsys.core.shard.specification;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FilterByTypeTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private FilterByType specification;
    private String type;

    @Before
    public void setUp() {
        type = "type1";
        specification = new FilterByType(type);
    }

    @Test
    public void testGetSql() {
        assertEquals("SELECT * FROM shard WHERE type LIKE ? ORDER BY ROWID ASC;", specification.getSql());
    }

    @Test
    public void testGetArgs() {
        assertArrayEquals(new String[]{type}, specification.getArgs());
    }
}