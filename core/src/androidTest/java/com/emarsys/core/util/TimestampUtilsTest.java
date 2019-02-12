package com.emarsys.core.util;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.version.VersionProvider;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TimestampUtilsTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
    }

    @Test
    public void testFormatTimestampWithUTC() throws ParseException {
        String deviceTimeZone = new DeviceInfo(InstrumentationRegistry.getTargetContext(),
                mock(HardwareIdProvider.class),
                mock(VersionProvider.class),
                mock(LanguageProvider.class)
        ).getTimezone();
        String dateString = "2017-12-07T10:46:09.100";
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        parser.setTimeZone(TimeZone.getTimeZone(deviceTimeZone));

        Date date = parser.parse(dateString);
        long timestamp = date.getTime();

        assertEquals(
                "2017-12-07T10:46:09.100Z",
                TimestampUtils.formatTimestampWithUTC(timestamp)
        );
    }
}