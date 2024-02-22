package com.emarsys.mobileengage

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MobileEngageRequestContextTest {

    private lateinit var requestContext: MobileEngageRequestContext

    @BeforeEach
    fun setUp() {

        requestContext = MobileEngageRequestContext(
            "appCode",
            1,
            null,
            null,
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock()
        )
    }

    @Test
    fun testHasContactIdentification_whenFalse() {
        requestContext.openIdToken = null

        requestContext.hasContactIdentification() shouldBe false
    }

    @Test
    fun testHasContactIdentification_whenHasOpenId_shouldBeTrue() {
        requestContext.openIdToken = "openId"

        requestContext.hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification__whenHasBothOpenIdAndContactFieldValue_shouldBeTrue() {
        requestContext.contactFieldValue = "contactFieldValue"

        requestContext.openIdToken = "openId"

        requestContext.hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_whenHasContactFieldValue_shouldBeTrue() {
        requestContext.contactFieldValue = "contactFieldValue"

        requestContext.openIdToken = null

        requestContext.hasContactIdentification() shouldBe true
    }
}