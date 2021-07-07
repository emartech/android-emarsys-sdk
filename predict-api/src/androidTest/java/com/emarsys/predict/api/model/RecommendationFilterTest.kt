package com.emarsys.predict.api.model;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;

public class RecommendationFilterTest {

    private static final String FIELD = "field";
    private static final String SINGLE_EXPECTATION = "singleExpectation";
    private static final List<String> MULTIPLE_EXPECTATIONS = Arrays.asList("expectation1", "expectation2");

    private static final String EXCLUDE_TYPE = "EXCLUDE";
    private static final String INCLUDE_TYPE = "INCLUDE";

    RecommendationFilter.Exclude exclude;
    RecommendationFilter.Include include;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        exclude = RecommendationFilter.exclude(FIELD);
        include = RecommendationFilter.include(FIELD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExclude_field_mustNotBeNull() {
        RecommendationFilter.exclude(null);
    }

    @Test
    public void testExclude_shouldReturn_withExcludeInstance() {
        Assert.assertEquals(RecommendationFilter.exclude(FIELD).getClass(), RecommendationFilter.Exclude.class);
    }

    @Test
    public void testExcludeConstructor_withField() {
        Assert.assertEquals(RecommendationFilter.exclude(FIELD).field, "field");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExclude_is_expectation_mustNotBeNull() {
        exclude.isValue(null);
    }

    @Test
    public void testExclude_is_shouldReturn_RecommendationFilter() {
        Assert.assertEquals(exclude.isValue("singleExpectation").getClass(), RecommendationFilter.class);
    }

    @Test
    public void testExclude_is_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "IS", SINGLE_EXPECTATION);
        RecommendationFilter result = exclude.isValue("singleExpectation");

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExclude_in_expectation_mustNotBeNull() {
        exclude.inValues(null);
    }

    @Test
    public void testExclude_in_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "IN", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = exclude.inValues(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testExclude_has_expectation_mustNotBeNull() {
        exclude.hasValue(null);
    }

    @Test
    public void testExclude_has_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "HAS", SINGLE_EXPECTATION);
        RecommendationFilter result = exclude.hasValue("singleExpectation");

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExclude_overlaps_expectation_mustNotBeNull() {
        exclude.overlapsValues(null);
    }

    @Test
    public void testExclude_overlaps_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "OVERLAPS", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = exclude.overlapsValues(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclude_field_mustNotBeNull() {
        RecommendationFilter.include(null);
    }


    @Test
    public void testInclude_shouldReturn_withIncludeInstance() {
        Assert.assertEquals(RecommendationFilter.include(FIELD).getClass(), RecommendationFilter.Include.class);
    }

    @Test
    public void testIncludeConstructor_withField() {
        Assert.assertEquals(RecommendationFilter.include(FIELD).field, "field");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclude_is_expectation_mustNotBeNull() {
        include.isValue(null);
    }

    @Test
    public void testInclude_is_shouldReturn_RecommendationFilter() {
        Assert.assertEquals(include.isValue("singleExpectation").getClass(), RecommendationFilter.class);
    }

    @Test
    public void testInclude_is_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "IS", SINGLE_EXPECTATION);
        RecommendationFilter result = include.isValue("singleExpectation");

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclude_in_expectation_mustNotBeNull() {
        include.inValues(null);
    }

    @Test
    public void testInclude_in_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "IN", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = include.inValues(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclude_has_expectation_mustNotBeNull() {
        include.hasValue(null);
    }

    @Test
    public void testInclude_has_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "HAS", SINGLE_EXPECTATION);
        RecommendationFilter result = include.hasValue("singleExpectation");

        Assert.assertEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInclude_overlaps_expectation_mustNotBeNull() {
        include.overlapsValues(null);
    }

    @Test
    public void testInclude_overlaps_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "OVERLAPS", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = include.overlapsValues(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }

}