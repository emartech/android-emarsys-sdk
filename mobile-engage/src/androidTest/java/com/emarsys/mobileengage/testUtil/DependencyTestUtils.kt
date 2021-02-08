package com.emarsys.mobileengage.testUtil

import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock

object DependencyTestUtils {
    fun setupDependencyInjectionWithServiceProviders() {
        val mockClientServiceProvider: ServiceEndpointProvider = mock {
            on { provideEndpointHost() } doReturn CLIENT_HOST
        }
        val mockEventServiceProvider: ServiceEndpointProvider = mock {
            on { provideEndpointHost() } doReturn EVENT_HOST
        }
        val mockMessageInboxServiceProvider: ServiceEndpointProvider = mock {
            on { provideEndpointHost() } doReturn INBOX_HOST
        }

        DependencyInjection.setup(
                FakeMobileEngageDependencyContainer(
                        clientServiceProvider = mockClientServiceProvider,
                        eventServiceProvider = mockEventServiceProvider,
                        messageInboxServiceProvider = mockMessageInboxServiceProvider
                ))
    }

    private const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
    private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
    private const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
}