package com.emarsys.mobileengage.inbox

import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.api.action.AppEventActionModel
import com.emarsys.mobileengage.api.action.CustomEventActionModel
import com.emarsys.mobileengage.api.action.DismissActionModel
import com.emarsys.mobileengage.api.action.OpenExternalUrlActionModel
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Message
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.net.URL

class MessageInboxResponseMapperTest  {

    private lateinit var messageInboxResponseMapper: MessageInboxResponseMapper

    @Before
    fun setUp() {
        messageInboxResponseMapper = MessageInboxResponseMapper()
    }

    @Test
    fun testMap_shouldReturnEmptyList_whenResponseBodyIsEmpty() {
        val mockResponseModel: ResponseModel = mock()
        val expected = InboxResult(listOf())

        val result = messageInboxResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_shouldReturnMessageInboxResult() {
        val expected = InboxResult(
            listOf(
                Message(
                    "messageId1",
                    "campaignId",
                    "collapseId",
                    "testMessage1",
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    "https://example.com/image.jpg",
                    142141412515,
                    142141412515,
                    50,
                    listOf("NEW"),
                    mapOf("key1" to "value1", "key2" to "value2"),
                    listOf(
                        AppEventActionModel(
                            "testId1",
                            "testTitle1",
                            "MEAppEvent",
                            "testName1",
                            mapOf(
                                "testKey1" to "testValue1",
                                "testKey2" to "testValue2",
                            )
                        ), AppEventActionModel(
                            "testId3",
                            "testTitle3",
                            "MEAppEvent",
                            "testName3",
                            emptyMap()
                        ),
                        OpenExternalUrlActionModel(
                            "testId2",
                            "testTitle2",
                            "OpenExternalUrl",
                            URL("https://www.test.com")
                        ),
                        OpenExternalUrlActionModel(
                            "testId4",
                            "testTitle2",
                            "OpenExternalUrl",
                            URL("https://")
                        )
                    )
                ),
                Message(
                    "messageId2",
                    "campaignId2",
                    "collapseId2",
                    "testMessage2",
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    "https://example.com/image.jpg",
                    142141412515,
                    142141412515,
                    50,
                    listOf(),
                    mapOf("key1" to "value1", "key2" to "value2"),
                    listOf(
                        CustomEventActionModel(
                            "testId3",
                            "testTitle3",
                            "MECustomEvent",
                            "testName3",
                            mapOf(
                                "testKey3" to "testValue3",
                                "testKey4" to "testValue4",
                            )
                        ),
                        DismissActionModel(
                            "testId4",
                            "testTitle4",
                            "Dismiss"
                        )
                    )
                ),
                Message(
                    "messageId3",
                    "campaignId3",
                    "collapseId3",
                    "testMessage3",
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    "https://example.com/image.jpg",
                    142141412515,
                    142141412515,
                    50,
                    listOf("READ", "DELETED"),
                    mapOf("key1" to "value1", "key2" to "value2"),
                    null
                ),
                Message(
                    "messageId4",
                    "campaignId4",
                    "collapseId4",
                    "testMessage4",
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    null,
                    142141412515,
                    142141412515,
                    50,
                    null,
                    null,
                    null
                )
            )
        )


        val result = messageInboxResponseMapper.map(createSuccessResponse())

        result shouldBe expected
    }

    private fun createSuccessResponse(): ResponseModel {
        val notificationString1 = """
        {
          "id": "messageId1",
          "multichannelId": 1,
          "campaignId": "campaignId",
          "collapseId": "collapseId",
          "title": "testMessage1",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "imageUrl": "https://example.com/image.jpg",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "expiresAt": 50,
          "tags": ["NEW"],
          "properties": {"key1":"value1", "key2":"value2"},
          "sourceId": 1234, 
          "sourceRunId": "1234",
          "sourceType": "push",
          "ems":{
              "actions": [
                {
                    "id": "testId1",
                    "title": "testTitle1",
                    "type": "MEAppEvent",
                    "name": "testName1",
                    "payload": {
                        "testKey1": "testValue1",
                        "testKey2": "testValue2"
                    }
                },
                {
                    "id": "testId3",
                    "title": "testTitle3",
                    "type": "MEAppEvent",
                    "name": "testName3",
                    "payload": null
                },
                {
                    "id": "testId2",
                    "title": "testTitle2",
                    "type": "OpenExternalUrl",
                    "url": "https://www.test.com"
                },
                {
                    "id": "testId4",
                    "title": "testTitle2",
                    "type": "OpenExternalUrl",
                    "url": "notUrl"
                }
              ]
          }
        }"""
        val notificationString2 = """
        {
          "id": "messageId2",
          "multichannelId": 1,
          "campaignId": "campaignId2",
          "collapseId": "collapseId2",
          "title": "testMessage2",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "imageUrl": "https://example.com/image.jpg",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "expiresAt": 50,
          "tags": [],
          "properties": {"key1":"value1", "key2":"value2"},
          "sourceId": 1234,
          "sourceRunId": "1234",
          "sourceType": "push",
          "ems": {
              "actions": [
                {
                    "id": "testId3",
                    "title": "testTitle3",
                    "type": "MECustomEvent",
                    "name": "testName3",
                    "payload": {
                        "testKey3": "testValue3",
                        "testKey4": "testValue4"
                    }
                },
                {
                    "id": "testId4",
                    "title": "testTitle4",
                    "type": "Dismiss"
                }
              ]
          }
        }"""
        val notificationString3 = """
        {
          "id": "messageId3",
          "multichannelId": 1,
          "campaignId": "campaignId3",
          "collapseId": "collapseId3",
          "title": "testMessage3",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "imageUrl": "https://example.com/image.jpg",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "expiresAt": 50,
          "tags": ["READ", "DELETED"],
          "properties": {"key1":"value1", "key2":"value2"},
          "sourceId": 1234,
          "sourceRunId": "1234",
          "sourceType": "push"
        }"""
        val notificationString4 = """
        {
          "id": "messageId4",
          "multichannelId": 1,
          "campaignId": "campaignId4",
          "collapseId": "collapseId4",
          "title": "testMessage4",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "expiresAt": 50,
          "sourceId": 1234,
          "sourceRunId": "1234",
          "sourceType": "push"
        }"""
        val json =
            """{"count": 3, "messages": [$notificationString1,$notificationString2,$notificationString3,$notificationString4]}"""
        return ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .body(json)
            .requestModel(mock())
            .build()
    }
}