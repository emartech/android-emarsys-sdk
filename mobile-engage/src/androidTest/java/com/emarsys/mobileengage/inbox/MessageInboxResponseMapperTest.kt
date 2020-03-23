package com.emarsys.mobileengage.inbox

import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.api.inbox.InboxMessage
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
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
        val expected = MessageInboxResult(listOf())

        val result = messageInboxResponseMapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_shouldReturnMessageInboxResult() {
        val expected = MessageInboxResult(listOf(
                InboxMessage(
                        "ef14afa4",
                        1,
                        "testMessage2",
                        "testMessage1",
                        "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        "https://example.com/image.jpg",
                        "https://example.com/image.jpg",
                        142141412515,
                        142141412515,
                        50,
                        listOf("NEW"),
                        1234,
                        "1234",
                        "push"
                ),
                InboxMessage(
                        "ef14afa4",
                        1,
                        "testMessage2",
                        "testMessage2",
                        "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        "https://example.com/image.jpg",
                        "https://example.com/image.jpg",
                        142141412515,
                        142141412515,
                        50,
                        listOf(),
                        1234,
                        "1234",
                        "push"
                ),
                InboxMessage(
                        "ef14afa4",
                        1,
                        "testMessage2",
                        "testMessage3",
                        "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        "https://example.com/image.jpg",
                        "https://example.com/image.jpg",
                        142141412515,
                        142141412515,
                        50,
                        listOf("READ", "DELETED"),
                        1234,
                        "1234",
                        "push"
                )))

        val result = messageInboxResponseMapper.map(createSuccessResponse())

        result shouldBe expected
    }

    private fun createSuccessResponse(): ResponseModel {
        val notificationString1 = """
        {
          "id": "ef14afa4",
          "multichannelId": 1,
          "campaignId": "testMessage2",
          "title": "testMessage1",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "imageUrl": "https://example.com/image.jpg",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "ttl": 50,
          "tags": ["NEW"],
          "sourceId": 1234, 
          "sourceRunId": "1234",
          "sourceType": "push"
        }"""
        val notificationString2 = """
        {
          "id": "ef14afa4",
          "multichannelId": 1,
          "campaignId": "testMessage2",
          "title": "testMessage2",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "imageUrl": "https://example.com/image.jpg",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "ttl": 50,
          "tags": [],
          "sourceId": 1234,
          "sourceRunId": "1234",
          "sourceType": "push"
        }"""
        val notificationString3 = """
        {
          "id": "ef14afa4",
          "multichannelId": 1,
          "campaignId": "testMessage2",
          "title": "testMessage3",
          "body": "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          "imageUrl": "https://example.com/image.jpg",
          "action": "https://example.com/image.jpg",
          "receivedAt": 142141412515,
          "updatedAt": 142141412515,
          "ttl": 50,
          "tags": ["READ", "DELETED"],
          "sourceId": 1234,
          "sourceRunId": "1234",
          "sourceType": "push"
        }"""
        val json = """{"count": 3, "notifications": [$notificationString1,$notificationString2,$notificationString3]}"""
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(json)
                .requestModel(mock())
                .build()
    }
}