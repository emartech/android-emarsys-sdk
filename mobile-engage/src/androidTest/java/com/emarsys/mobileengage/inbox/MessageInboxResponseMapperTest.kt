package com.emarsys.mobileengage.inbox

import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Message
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test

class MessageInboxResponseMapperTest {

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
        val expected = InboxResult(listOf(
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
                        mapOf("key1" to "value1", "key2" to "value2"
                        )),
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
                        mapOf("key1" to "value1", "key2" to "value2")),
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
                        mapOf("key1" to "value1", "key2" to "value2")),
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
                        null)
        ))


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
          "sourceType": "push"
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
          "sourceType": "push"
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
        val json = """{"count": 3, "messages": [$notificationString1,$notificationString2,$notificationString3,$notificationString4]}"""
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(json)
                .requestModel(mock())
                .build()
    }
}