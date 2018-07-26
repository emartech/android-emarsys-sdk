package com.emarsys.mobileengage.testUtil;

import com.emarsys.core.request.model.RequestModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestModelTestUtils {

    public static void assertEqualsRequestModels(RequestModel expected, RequestModel actual) {
        assertNotNull(actual);
        assertEquals(expected.getUrl(), actual.getUrl());
        assertEquals(expected.getMethod(), actual.getMethod());
        assertEquals(expected.getPayload(), actual.getPayload());
        assertEquals(expected.getHeaders(), actual.getHeaders());
        assertEquals(expected.getTtl(), actual.getTtl());
    }

}
