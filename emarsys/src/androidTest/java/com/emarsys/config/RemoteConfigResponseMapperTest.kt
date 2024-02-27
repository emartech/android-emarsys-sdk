package com.emarsys.config


import com.emarsys.common.feature.InnerFeature
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.random.RandomProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.log.LogLevel
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RemoteConfigResponseMapperTest : AnnotationSpec() {


    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRandomProvider: RandomProvider
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var remoteConfigResponseMapper: RemoteConfigResponseMapper


    @Before
    fun setup() {
        mockResponseModel = mock(ResponseModel::class.java)
        mockRandomProvider = mock(RandomProvider::class.java)
        mockHardwareIdProvider = mock {
            on { provideHardwareId() } doReturn "testHardwareId"
        }

        remoteConfigResponseMapper =
            RemoteConfigResponseMapper(mockRandomProvider, mockHardwareIdProvider)
    }

    @Test
    fun testMap_mapsResponseModel_to_RemoteConfig() {
        whenever(mockRandomProvider.provideDouble(1.0)).thenReturn(0.2)
        whenever(mockResponseModel.body).thenReturn(
                fullRemoteConfigJson.trimIndent()
        )

        val expected = RemoteConfig(
                "https://testEventService.emarsys.net",
                "https://testClientService.emarsys.net",
                "https://testPredictService.emarsys.net",
                "https://testMobileEngageV2Service.emarsys.net",
                "https://testDeepLinkService.emarsys.net",
                "https://testinboxService.emarsys.net",
                "https://testMessageInboxService.emarsys.net",
                LogLevel.INFO,
                mapOf(
                        InnerFeature.MOBILE_ENGAGE to true,
                        InnerFeature.PREDICT to false))

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_mapsResponseModel_to_RemoteConfig_withHwIdOverride() {
        whenever(mockRandomProvider.provideDouble(1.0)).thenReturn(0.6)
        whenever(mockResponseModel.body).thenReturn(fullRemoteConfigJson.trimIndent())
        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn("hardwareId1")

        val expected = RemoteConfig(
                "https://mobile-events-2.eservice.emarsys.net",
                "https://testClientService.emarsys.net",
                "https://testPredictService.emarsys.net",
                "https://testMobileEngageV2Service.emarsys.net",
                "https://testDeepLinkService.emarsys.net",
                "https://testinboxService.emarsys.net",
                "https://testMessageInboxService.emarsys.net",
                LogLevel.TRACE,
                mapOf(
                        InnerFeature.MOBILE_ENGAGE to false,
                        InnerFeature.PREDICT to true))

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
                                "inboxService":"https://testinboxService.emarsys.net"
                        }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(
                inboxServiceUrl = "https://testinboxService.emarsys.net")

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_mapsResponseModel_to_RemoteConfig_withFeaturesCamelCase() {
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "features":{
                            "mobileEngage":true,
                            "predict":false,
                            "notAValidFeature":false
                        }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(features = mapOf(
                InnerFeature.MOBILE_ENGAGE to true,
                InnerFeature.PREDICT to false))

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_mapsResponseModel_to_RemoteConfig_withFeaturesSnakeCase() {
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "features":{
                            "mobile_engage":true,
                            "predict":false,
                            "notAValidFeature":false
                        }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig(features = mapOf(
                InnerFeature.MOBILE_ENGAGE to true,
                InnerFeature.PREDICT to false))

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
                                "inboxService":"https://testinboxService.emarsys.net", 
                        }
                   }
               """.trimIndent()
        )

        val expected = RemoteConfig()

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun test_withHijackedUrl() {
        whenever(mockRandomProvider.provideDouble(1.0)).thenReturn(0.2)
        whenever(mockResponseModel.body).thenReturn(
                """
                   {
                        "serviceUrls":{
                                "eventService":"https://test-event.emarsys.com",
                                "clientService":"https://testClientService.url",
                                "predictService":"https://test-predict.emarsys.net/v1"
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
                eventServiceUrl = "https://test-event.emarsys.com",
                clientServiceUrl = null,
                predictServiceUrl = "https://test-predict.emarsys.net/v1",
                logLevel = LogLevel.INFO)

        val result = remoteConfigResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    companion object {
        const val fullRemoteConfigJson = """
                   {
                      "serviceUrls": {
                        "eventService": "https://testEventService.emarsys.net",
                        "clientService": "https://testClientService.emarsys.net",
                        "predictService": "https://testPredictService.emarsys.net",
                        "mobileEngageV2Service": "https://testMobileEngageV2Service.emarsys.net",
                        "deepLinkService": "https://testDeepLinkService.emarsys.net",
                        "inboxService": "https://testinboxService.emarsys.net",
                        "messageInboxService": "https://testMessageInboxService.emarsys.net"
                      },
                      "logLevel": "ERROR",
                      "luckyLogger": {
                        "logLevel": "INFO",
                        "threshold": 0.2
                      },
                       "features":{
                          "mobileEngage":true,
                          "predict":false,
                          "experimentalFeature1":false
                       },
                      "overrides": {
                        "hardwareId1": {
                          "serviceUrls": {
                            "eventService": "https://mobile-events-2.eservice.emarsys.net"
                          },
                          "logLevel": "TRACE",
                          "luckyLogger": {
                            "logLevel": "WARN",
                            "threshold": 0.3
                          },
                           "features":{
                              "mobileEngage":false,
                              "predict":true,
                              "experimentalFeature1":false
                           }
                        },
                        "hardwareId2": {
                          "serviceUrls": {
                            "eventService": "https://mobile-events-2.eservice.emarsys.net",
                            "clientService": "https://me-client.eservice.emarsys.net "
                          },
                          "logLevel": "DEBUG"
                        }
                      }
                    }
               """
    }
}