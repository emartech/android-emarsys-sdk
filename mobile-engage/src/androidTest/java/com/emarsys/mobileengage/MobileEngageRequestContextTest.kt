package com.emarsys.mobileengage

import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MobileEngageRequestContextTest : AnnotationSpec() {

    private lateinit var mockContactFieldValueStorage: Storage<String?>

    private lateinit var requestContext: MobileEngageRequestContext

    @Before
    fun setUp() {
        mockContactFieldValueStorage = mock<Storage<String?>>()
        requestContext = MobileEngageRequestContext(
            "appCode",
            1,
            null,
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mockContactFieldValueStorage,
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
        whenever(mockContactFieldValueStorage.get()).thenReturn("contactFieldValue")

        requestContext.openIdToken = null

        requestContext.hasContactIdentification() shouldBe true
    }

    @Test
    fun testContactFieldValue_setter_shouldDelegateToStorage() {
        val testContactFieldValue = "testContactFieldValue"

        requestContext.contactFieldValue = "testContactFieldValue"

        verify(mockContactFieldValueStorage).set(testContactFieldValue)
    }

    @Test
    fun testContactFieldValue_getter_shouldDelegateToStorage() {
        val testContactFieldValue = "testContactFieldValue"
        whenever(mockContactFieldValueStorage.get()).thenReturn(testContactFieldValue)

        requestContext.contactFieldValue shouldBe testContactFieldValue
        verify(mockContactFieldValueStorage).get()
    }

    @Test
    fun testReset() {
        requestContext.reset()

        requestContext.clientStateStorage.get() shouldBe null
        requestContext.contactTokenStorage.get() shouldBe null
        requestContext.refreshTokenStorage.get() shouldBe null
        requestContext.contactFieldValueStorage.get() shouldBe null
        requestContext.pushTokenStorage.get() shouldBe null
        requestContext.sessionIdHolder.sessionId shouldBe null
        requestContext.openIdToken shouldBe null
        requestContext.applicationCode shouldBe null
    }
}