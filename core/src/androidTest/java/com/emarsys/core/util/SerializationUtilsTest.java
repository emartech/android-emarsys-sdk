package com.emarsys.core.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.serialization.SerializationException;
import com.emarsys.core.util.serialization.SerializationUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class SerializationUtilsTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    public void testSerialization() throws SerializationException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("key", "value");
        HashMap<String, Object> nested = new HashMap<>();
        nested.put("key2", "value2");
        nested.put("key3", true);
        payload.put("nested", nested);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("header1", "header-value1");
        headers.put("header2", "header-value2");

        RequestModel expected = new RequestModel("https://www.google.com", RequestMethod.GET, payload, headers, 999, 101, "id");

        byte[] blob = SerializationUtils.serializableToBlob(expected);
        RequestModel result = (RequestModel) SerializationUtils.blobToSerializable(blob);

        assertEquals(expected, result);
    }

    @Test
    public void testSerialization_serializesNullCorrectly() throws SerializationException {
        RequestModel requestModel = null;
        byte[] bytes = SerializationUtils.serializableToBlob(requestModel);
        RequestModel result = (RequestModel) SerializationUtils.blobToSerializable(bytes);
        assertNull(result);
    }

}