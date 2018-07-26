package com.emarsys.core.request.model;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class CompositeRequestModelTest {

    public static final int TIMESTAMP = 800;
    public static final int TTL = 1000;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    public void testEquals_withEqualModels() {
        CompositeRequestModel model1 = new CompositeRequestModel(
                "https://google.com",
                RequestMethod.GET,
                new HashMap<String, Object>() {{
                    put("payload_key1", 6);
                    put("payload_key2", false);
                    put("payload_key3", "value");
                }},
                new HashMap<String, String>() {{
                    put("header_key1", "value1");
                    put("header_key2", "value2");
                    put("header_key3", "value3");
                }},
                TIMESTAMP,
                TTL,
                new String[]{"child_id1", "child_id2", "child_id3", "child_id4"});

        CompositeRequestModel model2 = new CompositeRequestModel(
                "https://google.com",
                RequestMethod.GET,
                new HashMap<String, Object>() {{
                    put("payload_key1", 6);
                    put("payload_key2", false);
                    put("payload_key3", "value");
                }},
                new HashMap<String, String>() {{
                    put("header_key1", "value1");
                    put("header_key2", "value2");
                    put("header_key3", "value3");
                }},
                TIMESTAMP,
                TTL,
                new String[]{"child_id1", "child_id2", "child_id3", "child_id4"});

        assertEquals(model1, model2);
    }

    @Test
    public void testEquals_withDifferentChildIds() {
        String url = "https://google.com";
        RequestMethod method = RequestMethod.GET;
        HashMap<String, Object> payload = new HashMap<String, Object>() {{
            put("payload_key1", 6);
            put("payload_key2", false);
            put("payload_key3", "value");
        }};
        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("header_key1", "value1");
            put("header_key2", "value2");
            put("header_key3", "value3");
        }};

        CompositeRequestModel model1 = new CompositeRequestModel(
                url,
                method,
                payload,
                headers,
                TIMESTAMP,
                TTL,
                new String[]{"child_id4"});

        CompositeRequestModel model2 = new CompositeRequestModel(
                url,
                method,
                payload,
                headers,
                TIMESTAMP,
                TTL,
                new String[]{"child_id1", "child_id2", "child_id3"});

        assertThat(model1, not(equalTo(model2)));
    }

}