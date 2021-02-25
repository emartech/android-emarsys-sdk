package com.emarsys.mobileengage

import com.emarsys.core.storage.StringStorage
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test

class MobileEngageRequestContextTest {

    private lateinit var mockContactFieldValueStorage: StringStorage
    private lateinit var requestContext: MobileEngageRequestContext

    @Before
    fun setUp() {
        mockContactFieldValueStorage = mock {
            on { get() } doReturn null
        }
        requestContext = MobileEngageRequestContext("appCode", 1, null,
                mock(), mock(), mock(), mock(), mock(), mock(), mockContactFieldValueStorage, mock(), mock())
    }

    @Test
    fun testHasContactIdentification_whenFalse() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)
        requestContext.openIdToken = null

        requestContext.hasContactIdentification() shouldBe false
    }

    @Test
    fun testHasContactIdentification_whenHasOpenId_shouldBeTrue() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)
        requestContext.openIdToken = "openId"

        requestContext.hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification__whenHasBothOpenIdAndContactFieldValue_shouldBeTrue() {
        whenever(mockContactFieldValueStorage.get()).thenReturn("contactFieldValue")
        requestContext.openIdToken = "openId"

        requestContext.hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_whenHasContactFieldValue_shouldBeTrue() {
        whenever(mockContactFieldValueStorage.get()).thenReturn("contactFieldValue")
        requestContext.openIdToken = null

        requestContext.hasContactIdentification() shouldBe true
    }
}