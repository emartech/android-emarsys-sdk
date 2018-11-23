package com.emarsys.mobileengage.storage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.testUtil.TimeoutUtils;

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
        storage = createMeIdStorage();
        storage.remove();
    }

    @Test
    public void testGet_shouldReturnNull_ifTheStorageIsEmpty() {
        assertNull(storage.get());
    }

    @Test
    public void testSet() {
        storage.set("12345");
        assertEquals("12345", storage.get());
    }

    @Test
    public void testRemove_shouldRemoveMeId() {
        storage.set("12345");
        storage.remove();

        assertNull(storage.get());
    }

    @Test
    public void testSet_shouldPreserveMeId() {
        storage.set("12345");
        storage = createMeIdStorage();

        assertEquals("12345", storage.get());
    }

    private MeIdStorage createMeIdStorage() {
        return new MeIdStorage(context.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE));
    }

}