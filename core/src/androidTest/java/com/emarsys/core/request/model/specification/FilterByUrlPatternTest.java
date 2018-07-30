package com.emarsys.core.request.model.specification;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class FilterByUrlPatternTest {

    private FilterByUrlPattern specification;
    private String pattern;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        pattern = "root/___/_%/event";
        specification = new FilterByUrlPattern(pattern);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructor_patternMustNotBeNull(){
        new FilterByUrlPattern(null);
    }

    @Test
    public void testGetSql() {
        assertEquals("SELECT * FROM request WHERE url LIKE ?;", specification.getSql());
    }

    @Test
    public void testGetArs() {
        assertArrayEquals(new String[]{pattern}, specification.getArgs());
    }
}