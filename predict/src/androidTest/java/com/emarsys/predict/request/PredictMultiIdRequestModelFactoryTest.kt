package com.emarsys.predict.request

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class PredictMultiIdRequestModelFactoryTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        const val CONTACT_FIELD_ID = 3
        const val CONTACT_FIELD_VALUE = "contactFieldValue"
    }

    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var testPredictRequestContext: PredictRequestContext
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider

    private lateinit var factory: PredictMultiIdRequestModelFactory

    @Before
    fun setup() {
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }

        mockUUIDProvider = mock {
            on { provideId() } doReturn REQUEST_ID
        }

        testPredictRequestContext = PredictRequestContext(
            "merchant_id",
            mock(),
            mockTimestampProvider,
            mockUUIDProvider,
            mock()
        )
        mockClientServiceProvider = mock {
            on { provideEndpointHost() } doReturn CLIENT_HOST
        }

        factory =
            PredictMultiIdRequestModelFactory(testPredictRequestContext, mockClientServiceProvider)
    }

    @Test
    fun testCreateSetContactRequestModel_whenMerchantId_isPresentOnContext() {
        val expected = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/contact-token",
            RequestMethod.POST,
            mapOf(
                "contactFieldId" to CONTACT_FIELD_ID,
                "contactFieldValue" to CONTACT_FIELD_VALUE
            ),
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

        val requestModel =
            factory.createSetContactRequestModel(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        requestModel shouldBe expected
    }

    @Test
    fun testCreateSetContactRequestModel_whenMerchantId_isMissingFromContext_throws_IllegalArgumentException() {
        testPredictRequestContext.merchantId = null

        shouldThrow<IllegalArgumentException> {
            factory.createSetContactRequestModel(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }
}