package com.emarsys.mobileengage.service

import android.content.Context
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperV1
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RemoteMessageMapperV1Test {
    private companion object {
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val IMAGE_URL = "https://emarsys.com/image"
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456
        const val SID = "test sid"

        const val METADATA_SMALL_NOTIFICATION_ICON_KEY =
            "com.emarsys.mobileengage.small_notification_icon"
        const val METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color"
        val DEFAULT_SMALL_NOTIFICATION_ICON = R.drawable.default_small_notification_icon
    }

    private lateinit var context: Context
    private lateinit var mockMetaDataReader: MetaDataReader
    private lateinit var remoteMessageMapperV1: RemoteMessageMapperV1


    @BeforeEach
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        val uuidProvider: UUIDProvider = mock {
            on { provideId() }.thenReturn("testUUID")
        }
        mockMetaDataReader = mock()

        remoteMessageMapperV1 = RemoteMessageMapperV1(
            mockMetaDataReader,
            context,
            uuidProvider
        )
    }

    @AfterEach
    fun tearDown() {
        mobileEngage().concurrentHandlerHolder.coreLooper.quitSafely()
        tearDownMobileEngageComponent()
    }

    @Test
    fun testMap() {
        whenever(
            mockMetaDataReader.getInt(
                context,
                METADATA_SMALL_NOTIFICATION_ICON_KEY,
                DEFAULT_SMALL_NOTIFICATION_ICON
            )
        )
            .thenReturn(SMALL_RESOURCE_ID)
        whenever(mockMetaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR))
            .thenReturn(COLOR_RESOURCE_ID)

        val ems = JSONObject()
        ems.put("style", "THUMBNAIL")
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["channel_id"] = CHANNEL_ID
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.body shouldBe BODY
        notificationData.title shouldBe TITLE
        notificationData.channelId shouldBe CHANNEL_ID
        notificationData.smallIconResourceId shouldBe SMALL_RESOURCE_ID
        notificationData.colorResourceId shouldBe COLOR_RESOURCE_ID
        notificationData.style shouldBe "THUMBNAIL"
    }

    @Test
    fun testMap_whenTitleIsMissing() {
        val input: MutableMap<String, String?> = createRemoteMessage(title = null)
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.body shouldBe BODY
        notificationData.title shouldBe null
        notificationData.channelId shouldBe CHANNEL_ID
    }

    @Test
    fun testMap_whenMapIsEmpty() {
        val input: MutableMap<String, String?> = HashMap()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsAvailable() {
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.imageUrl shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsNotAvailable() {
        val testImageUrl = "https://fa.il/img.jpg"
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = testImageUrl
        input["channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.imageUrl shouldBe testImageUrl
    }

    @Test
    fun testMap_whenNotificationMethodIsSet() {
        val collapseId = "testNotificationId"

        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("collapseId", collapseId)
        notificationMethodJson.put("operation", "UPDATE")
        val ems = JSONObject()
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.collapseId shouldBe collapseId
        notificationData.operation shouldBe NotificationOperation.UPDATE.name
    }

    @Test
    fun testMap_whenNotificationMethodIsMissing() {
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.operation shouldBe NotificationOperation.INIT.name
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withoutCollapseID_shouldReturnWithInitOperation() {
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("operation", "UPDATE")
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.operation shouldBe NotificationOperation.INIT.name
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withCollapseID_shouldReturnWithSetOperation() {
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("collapseId", 123)
        notificationMethodJson.put("operation", "UPDATE")
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.operation shouldBe NotificationOperation.UPDATE.name
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withCollapseID_shouldReturnWithInitOperation_whenOperationIsMissing() {
        val input: MutableMap<String, String?> = createRemoteMessage()
        input["image_url"] = IMAGE_URL
        input["channel_id"] = CHANNEL_ID
        val ems = JSONObject()
        val notificationMethodJson = JSONObject()
        notificationMethodJson.put("collapseId", 123)
        ems.put("notificationMethod", notificationMethodJson)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.operation shouldBe NotificationOperation.INIT.name
    }

    @Test
    fun testMap_notificationData_shouldContain_campaignId() {
        val testCampaignId = "test campaign id"
        val input: MutableMap<String, String?> = createRemoteMessage()
        val ems = JSONObject()
        ems.put("multichannelId", testCampaignId)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.campaignId shouldBe testCampaignId
    }

    @Test
    fun testMap_notificationData_shouldContain_sid() {
        val input: MutableMap<String, String?> = createRemoteMessage()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.sid shouldBe SID
    }

    @Test
    fun testMap_notificationData_shouldContain_actions() {
        val testActions = "test actions"
        val input: MutableMap<String, String?> = createRemoteMessage()
        val ems = JSONObject()
        ems.put("actions", testActions)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.actions shouldBe testActions
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifActionsAreMissing() {
        val input: MutableMap<String, String?> = createRemoteMessage()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.actions shouldBe null
    }

    @Test
    fun testMap_notificationData_shouldContain_defaultAction() {
        val testDefaultAction = "test default action"
        val input: MutableMap<String, String?> = createRemoteMessage()
        val ems = JSONObject()
        ems.put("default_action", testDefaultAction)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.defaultAction shouldBe testDefaultAction
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifDefaultActionIsMissing() {
        val input: MutableMap<String, String?> = createRemoteMessage()
        val ems = JSONObject()
        input["ems"] = ems.toString()
        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.defaultAction shouldBe null
    }

    @Test
    fun testMap_notificationData_shouldContain_inapp() {
        val testInapp = "test inapp"
        val input: MutableMap<String, String?> = createRemoteMessage()
        val ems = JSONObject()
        ems.put("inapp", testInapp)
        input["ems"] = ems.toString()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.inapp shouldBe testInapp
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifInappIsMissing() {
        val input: MutableMap<String, String?> = createRemoteMessage()

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.inapp shouldBe null
    }

    @Test
    fun testMap_notificationData_should_add_remaining_properties_as_rootParam() {
        val rootParam = mapOf(
            "key1" to "value1",
            "key2" to "value2",
        )
        val input: MutableMap<String, String?> = createRemoteMessage(rootParam = rootParam)

        val notificationData = remoteMessageMapperV1.map(input)

        notificationData.rootParams shouldBe rootParam
    }

    private fun createRemoteMessage(
        title: String? = TITLE,
        body: String = BODY,
        sid: String = SID,
        rootParam: Map<String, String?> = mapOf()
    ): MutableMap<String, String?> {
        val payload = mutableMapOf<String, String?>()
        title?.let { payload["title"] = it }
        payload["body"] = body

        val uObject = """{"sid":"$sid"}"""
        payload["u"] = uObject

        return (payload + rootParam).toMutableMap()
    }
}