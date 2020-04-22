package com.emarsys.config

import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.provider.random.RandomProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.log.LogLevel
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class RemoteConfigResponseMapperTest {


    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRandomProvider: RandomProvider
    private lateinit var remoteConfigResponseMapper: RemoteConfigResponseMapper


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        mockResponseModel = mock(ResponseModel::class.java)
        mockRandomProvider = mock(RandomProvider::class.java)

        remoteConfigResponseMapper = RemoteConfigResponseMapper(mockRandomProvider)
    }

    @Test
    fun testMap_mapsResponseModel_to_RemoteConfig() {
        whenever(mockRandomProvider.provideDouble(1.0)).thenReturn(0.2)
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "serviceUrls":{
                                "eventService":"https://testEventService.url",
                                "clientService":"https://testClientService.url",
                                "predictService":"https://testPredictService.url",
                                "mobileEngageV2Service":"https://testMobileEngageV2Service.url",
                                "deepLinkService":"https://testDeepLinkService.url",
                                "inboxService":"https://testinboxService.url",
                                "messageInboxService":"https://testMessageInboxService.url"
                        },
                        "logLevel": "ERROR",
                        "luckyLogger": {
                               "logLevel": "INFO",
                               "threshold": 0.2
                           }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(
                "https://testEventService.url",
                "https://testClientService.url",
                "https://testPredictService.url",
                "https://testMobileEngageV2Service.url",
                "https://testDeepLinkService.url",
                "https://testinboxService.url",
                "https://testMessageInboxService.url",
                LogLevel.INFO)

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_withZeroLuckyThreshold() {
        whenever(mockRandomProvider.provideDouble(1.0)).thenReturn(0.0)
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "logLevel": "ERROR",
                        "luckyLogger": {
                               "logLevel": "INFO",
                               "threshold": 0
                           }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(logLevel = LogLevel.ERROR)

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_withMaximumThreshold() {
        whenever(mockRandomProvider.provideDouble(1.0)).thenReturn(1.0)
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "logLevel": "ERROR",
                        "luckyLogger": {
                               "logLevel": "INFO",
                               "threshold": 1
                           }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(logLevel = LogLevel.INFO)

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_mapsResponseModel_to_RemoteConfig_withSomeElements() {
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "serviceUrls":{
                                "inboxService":"https://testinboxService.url"
                        }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(
                inboxServiceUrl = "https://testinboxService.url")

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun test_withEmptyJSON() {
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig()

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun test_withInvalidJSON() {
        whenever(mockResponseModel.body).thenReturn(
                """
                   {x
                        "serviceUrls":{
                                "inboxService":"https://testinboxService.url", 
                        }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig()

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }
}