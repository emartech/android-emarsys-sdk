package com.emarsys.predict.api.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecommendationFilterTest {

    public static final String FIELD = "field";
    public static final List<String> SINGLE_EXPECTATION = Collections.singletonList("singleExpectation");
    public static final List<String> MULTIPLE_EXPECTATIONS = Arrays.asList("expectation1", "expectation2");

    public static final String EXCLUDE_TYPE = "exclude";
    public static final String INCLUDE_TYPE = "include";

    RecommendationFilter.Exclude exclude;
    RecommendationFilter.Include include;

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
        exclude.is(null);
    }

    @Test
    public void testExclude_is_shouldReturn_RecommendationFilter() {
        Assert.assertEquals(exclude.is("singleExpectation").getClass(), RecommendationFilter.class);
    }

    @Test
    public void testExclude_is_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "is", SINGLE_EXPECTATION);
        RecommendationFilter result = exclude.is("singleExpectation");

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExclude_in_expectation_mustNotBeNull() {
        exclude.in(null);
    }

    @Test
    public void testExclude_in_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "in", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = exclude.in(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testExclude_has_expectation_mustNotBeNull() {
        exclude.has(null);
    }

    @Test
    public void testExclude_has_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "has", SINGLE_EXPECTATION);
        RecommendationFilter result = exclude.has("singleExpectation");

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExclude_overlaps_expectation_mustNotBeNull() {
        exclude.overlaps(null);
    }

    @Test
    public void testExclude_overlaps_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(EXCLUDE_TYPE, FIELD, "overlaps", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = exclude.overlaps(Arrays.asList("expectation1", "expectation2"));

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
        include.is(null);
    }

    @Test
    public void testInclude_is_shouldReturn_RecommendationFilter() {
        Assert.assertEquals(include.is("singleExpectation").getClass(), RecommendationFilter.class);
    }

    @Test
    public void testInclude_is_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "is", SINGLE_EXPECTATION);
        RecommendationFilter result = include.is("singleExpectation");

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclude_in_expectation_mustNotBeNull() {
        include.in(null);
    }

    @Test
    public void testInclude_in_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "in", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = include.in(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclude_has_expectation_mustNotBeNull() {
        include.has(null);
    }

    @Test
    public void testInclude_has_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "has", SINGLE_EXPECTATION);
        RecommendationFilter result = include.has("singleExpectation");

        Assert.assertEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInclude_overlaps_expectation_mustNotBeNull() {
        include.overlaps(null);
    }

    @Test
    public void testInclude_overlaps_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        RecommendationFilter expected = new RecommendationFilter(INCLUDE_TYPE, FIELD, "overlaps", MULTIPLE_EXPECTATIONS);
        RecommendationFilter result = include.overlaps(Arrays.asList("expectation1", "expectation2"));

        Assert.assertEquals(expected, result);
    }

}