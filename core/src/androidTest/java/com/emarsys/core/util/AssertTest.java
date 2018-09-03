package com.emarsys.core.util;

import com.emarsys.test.util.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AssertTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testNotNull_shouldThrowException_whenArgumentIsNull() {
        Assert.notNull(null, "");
    }

    @Test
    public void testNotNull_shouldNotThrowException_whenArgumentIsNotNull() {
        try {
            Assert.notNull("", null);
        } catch (Exception e) {
            fail("testNotNull should not throw expection when object is not null: " + e.getMessage());
        }
    }

    @Test
    public void testNotNull_shouldThrowException_withMessage_whenArgumentIsNull() {
        String message = "message";
        try {
            Assert.notNull(null, message);
            fail("Should throw exception");
        } catch (Exception e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testElementsNotNull_array_shouldThrowException_whenArgumentIsNull() {
        Assert.elementsNotNull((Object[]) null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testElementsNotNull_array_shouldThrowException_whenContainsNullElement() {
        Assert.elementsNotNull(new Object[]{
                new Object(),
                new Object(),
                null,
                new Object()
        }, "");
    }

    @Test
    public void testElementsNotNull_array_shouldThrowException_whenContainsNullElement_withSpecifiedMessage() {
        String message = "message";
        try {
            Assert.elementsNotNull(new Object[]{
                    new Object(),
                    new Object(),
                    null,
                    new Object()
            }, message);
            fail("Should throw exception");
        } catch (IllegalArgumentException iae) {
            assertEquals(message, iae.getMessage());
        }
    }

    @Test
    public void testElementsNotNull_array_shouldNotThrowException_whenArrayLacksNullElement() {
        try {
            Assert.elementsNotNull(new Object[]{new Object(), new Object(), "randomstring"}, null);
        } catch (Exception e) {
            fail("testElementsNotNull should not throw expection when object is not null: " + e.getMessage());
        }
    }

    @Test
    public void testElementsNotNull_array_shouldNotThrowException_whenArray_isEmpty() {
        try {
            Assert.elementsNotNull(new Object[]{}, null);
        } catch (Exception e) {
            fail("testElementsNotNull should not throw expection when object is not null: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testElementsNotNull_list_shouldThrowException_whenArgumentIsNull() {
        Assert.elementsNotNull((List) null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testElementsNotNull_list_shouldThrowException_whenContainsNullElement() {
        Assert.elementsNotNull(Arrays.asList(
                new Object(),
                new Object(),
                null,
                new Object()
        ), "");
    }

    @Test
    public void testElementsNotNull_list_shouldThrowException_whenContainsNullElement_withSpecifiedMessage() {
        String message = "message";
        try {
            Assert.elementsNotNull(Arrays.asList(
                    new Object(),
                    new Object(),
                    null,
                    new Object()
            ), message);
            fail("Should throw exception");
        } catch (IllegalArgumentException iae) {
            assertEquals(message, iae.getMessage());
        }
    }

    @Test
    public void testElementsNotNull_list_shouldNotThrowException_whenListLacksNullElement() {
        try {
            Assert.elementsNotNull(Arrays.asList(new Object(), new Object(), "randomstring"), null);
        } catch (Exception e) {
            fail("testElementsNotNull should not throw expection when object is not null: " + e.getMessage());
        }
    }

    @Test
    public void testElementsNotNull_list_shouldNotThrowException_whenList_isEmpty() {
        try {
            Assert.elementsNotNull(new ArrayList<>(), null);
        } catch (Exception e) {
            fail("testElementsNotNull should not throw expection when object is not null: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmpty_array_shouldNotAcceptNull() {
        Assert.notEmpty(null, "message");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmpty_array_shouldThrowException_whenArrayIsEmpty() {
        Assert.notEmpty(new Object[]{}, "");
    }

    @Test
    public void testNotEmpty_array_shouldThrowException_whenArrayIsEmpty_withSpecifiedMessage() {
        String message = "message";
        try {
            Assert.notEmpty(new Object[]{}, message);
            fail("Should throw exception");
        } catch (IllegalArgumentException iae) {
            assertEquals(message, iae.getMessage());
        }
    }

    @Test
    public void testNotEmpty_array_shouldThrowException_whenArrayIsEmpty_withDefaultMessage() {
        try {
            Assert.notEmpty(new Object[]{}, null);
            fail("Should throw exception");
        } catch (IllegalArgumentException iae) {
            assertEquals("Argument must not be empty!", iae.getMessage());
        }
    }

    @Test
    public void testNotEmpty_array_shouldNotThrowException_whenArrayHasItems() {
        try {
            Assert.notEmpty(new Object[]{"a", 3, Math.PI}, null);
        } catch (Exception e) {
            fail("testElementsNotNull should not throw expection when object is not null: " + e.getMessage());
        }
    }

}
