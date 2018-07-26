package com.emarsys.mobileengage.storage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class MeIdStorageTest {
    private MeIdStorage storage;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        storage = new MeIdStorage(context);
        storage.remove();
    }

    @Test
    public void get_shouldReturnNull_ifTheStorageIsEmpty() throws Exception {
        assertNull(storage.get());
    }

    @Test
    public void set() throws Exception {
        storage.set("12345");
        assertEquals("12345", storage.get());
    }

    @Test
    public void remove_shouldRemoveMeId() {
        storage.set("12345");
        storage.remove();

        assertNull(storage.get());
    }

    @Test
    public void set_shouldPreserveMeId() throws Exception {
        storage.set("12345");
        storage = new MeIdStorage(context);

        assertEquals("12345", storage.get());
    }

}