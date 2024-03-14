package com.emarsys.mobileengage

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class MobileEngageRequestContextTest : AnnotationSpec() {

    private lateinit var requestContext: MobileEngageRequestContext

    @Before
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