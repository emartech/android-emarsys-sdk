package com.emarsys.core.util


import com.emarsys.testUtil.AnnotationSpec
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class AssertTest : AnnotationSpec() {


    @Test
    fun testNotNull_shouldThrowException_whenArgumentIsNull() {
        shouldThrow<IllegalArgumentException> {
            Assert.notNull(null, "")
        }
    }

    @Test
    fun testNotNull_shouldNotThrowException_whenArgumentIsNotNull() {
        try {
            Assert.notNull("", null)
        } catch (e: Exception) {
            fail("testNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testNotNull_shouldThrowException_withMessage_whenArgumentIsNull() {
        val message = "message"
        try {
            Assert.notNull(null, message)
            fail("Should throw exception")
        } catch (e: Exception) {
            e.message shouldBe message
        }
    }

    @Test
    fun testElementsNotNull_array_shouldThrowException_whenArgumentIsNull() {
        shouldThrow<IllegalArgumentException> {
            Assert.elementsNotNull(null as Array<Any>?, "")
        }
    }

    @Test
    fun testElementsNotNull_array_shouldThrowException_whenContainsNullElement() {
        shouldThrow<IllegalArgumentException> {
            Assert.elementsNotNull(arrayOf(Any(), Any(), null, Any()), "")
        }
    }

    @Test
    fun testElementsNotNull_array_shouldThrowException_whenContainsNullElement_withSpecifiedMessage() {
        val message = "message"
        try {
            Assert.elementsNotNull(arrayOf(Any(), Any(), null, Any()), message)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe message
        }
    }

    @Test
    fun testElementsNotNull_array_shouldNotThrowException_whenArrayLacksNullElement() {
        try {
            Assert.elementsNotNull(arrayOf(Any(), Any(), "randomstring"), null)
        } catch (e: Exception) {
            fail("testElementsNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testElementsNotNull_array_shouldNotThrowException_whenArray_isEmpty() {
        try {
            Assert.elementsNotNull(arrayOf(), null)
        } catch (e: Exception) {
            fail("testElementsNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testElementsNotNull_list_shouldThrowException_whenArgumentIsNull() {
        shouldThrow<IllegalArgumentException> {
            Assert.elementsNotNull(null as List<*>?, "")
        }
    }

    @Test
    fun testElementsNotNull_list_shouldThrowException_whenContainsNullElement() {
        shouldThrow<IllegalArgumentException> {
            Assert.elementsNotNull(listOf(Any(), Any(), null, Any()), "")
        }
    }

    @Test
    fun testElementsNotNull_list_shouldThrowException_whenContainsNullElement_withSpecifiedMessage() {
        val message = "message"
        try {
            Assert.elementsNotNull(listOf(Any(), Any(), null, Any()), message)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe message
        }
    }

    @Test
    fun testElementsNotNull_list_shouldNotThrowException_whenListLacksNullElement() {
        try {
            Assert.elementsNotNull(listOf(Any(), Any(), "randomstring"), null)
        } catch (e: Exception) {
            fail("testElementsNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testElementsNotNull_list_shouldNotThrowException_whenList_isEmpty() {
        try {
            Assert.elementsNotNull(ArrayList<Any>(), null)
        } catch (e: Exception) {
            fail("testElementsNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testNotEmpty_array_shouldNotAcceptNull() {
        shouldThrow<IllegalArgumentException> {
            Assert.notEmpty(null as Array<Any>?, "message")
        }
    }

    @Test
    fun testNotEmpty_array_shouldThrowException_whenArrayIsEmpty() {
        shouldThrow<IllegalArgumentException> {
            Assert.notEmpty(arrayOf(), "")
        }
    }

    @Test
    fun testNotEmpty_array_shouldThrowException_whenArrayIsEmpty_withSpecifiedMessage() {
        val message = "message"
        try {
            Assert.notEmpty(arrayOf(), message)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe message
        }
    }

    @Test
    fun testNotEmpty_array_shouldThrowException_whenArrayIsEmpty_withDefaultMessage() {
        try {
            Assert.notEmpty(arrayOf(), null)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe "Argument must not be empty!"
        }
    }

    @Test
    fun testNotEmpty_array_shouldNotThrowException_whenArrayHasItems() {
        try {
            Assert.notEmpty(arrayOf<Any>("a", 3, Math.PI), null)
        } catch (e: Exception) {
            fail("testElementsNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testNotEmpty_list_shouldNotAcceptNull() {
        shouldThrow<IllegalArgumentException> {
            Assert.notEmpty(null as List<*>?, "message")
        }
    }


    @Test
    fun testNotEmpty_list_shouldThrowException_whenArrayIsEmpty() {
        shouldThrow<IllegalArgumentException> {
            Assert.notEmpty(listOf<Any>(), "")
        }
    }

    @Test
    fun testNotEmpty_list_shouldThrowException_whenArrayIsEmpty_withSpecifiedMessage() {
        val message = "message"
        try {
            Assert.notEmpty(listOf<Any>(), message)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe message
        }
    }

    @Test
    fun testNotEmpty_list_shouldThrowException_whenArrayIsEmpty_withDefaultMessage() {
        try {
            Assert.notEmpty(listOf<Any>(), null)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe "Argument must not be empty!"
        }
    }

    @Test
    fun testNotEmpty_list_shouldNotThrowException_whenArrayHasItems() {
        try {
            Assert.notEmpty(listOf("a", 3, Math.PI), null)
        } catch (e: Exception) {
            fail("testElementsNotNull should not throw exception when object is not null: " + e.message)
        }
    }

    @Test
    fun testPositiveInt_shouldThrowException_whenArgumentIsZero() {
        try {
            Assert.positiveInt(0, null)
            fail("Should throw exception")
        } catch (iae: IllegalArgumentException) {
            iae.message shouldBe "Argument must be greater than zero!"
        }
    }

    @Test
    fun testPositiveInt_shouldThrowException_whenArgumentIsNull() {
        try {
            Assert.positiveInt(null, null)
            fail("Should throw exception")
        } catch (iae: java.lang.IllegalArgumentException) {
            iae.message shouldBe "Argument must not be null!"
        }
    }

    @Test
    fun testPositiveInt_shouldThrowException_whenArgumentIsNegative() {
        try {
            Assert.positiveInt(-10, null)
            fail("Should throw exception")
        } catch (iae: java.lang.IllegalArgumentException) {
            iae.message shouldBe "Argument must be greater than zero!"
        }
    }

    @Test
    fun testPositiveInt_shouldNotThrowException_whenArgumentIsPositive() {
        try {
            Assert.positiveInt(3, null)
        } catch (e: Exception) {
            fail("positiveInt should not throw exception when argument is greater than zero: " + e.message)
        }
    }
}
