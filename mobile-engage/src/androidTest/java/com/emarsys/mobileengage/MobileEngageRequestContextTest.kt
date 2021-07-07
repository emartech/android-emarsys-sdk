package com.emarsys.mobileengage

import com.emarsys.core.storage.StringStorage
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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