package com.emarsys.mobileengage.service


import android.content.Context
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperV2
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.json.JSONArray
import org.json.JSONObject
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


class RemoteMessageMapperV2Test : AnnotationSpec() {
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
    private lateinit var remoteMessageMapperV2: RemoteMessageMapperV2


    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()

        mockMetaDataReader = mock()

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        val uuidProvider: UUIDProvider = mock {
            on { provideId() }.thenReturn("testUUID")
        }

        remoteMessageMapperV2 = RemoteMessageMapperV2(
            mockMetaDataReader,
            context,
            uuidProvider
        )
    }

    @After
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
        whenever(
            mockMetaDataReader.getInt(
                context,
                METADATA_NOTIFICATION_COLOR
            )
        )
            .thenReturn(COLOR_RESOURCE_ID)

        val input: MutableMap<String, String> = createRemoteMessage()
        input["notification.channel_id"] = CHANNEL_ID
        input["ems.style"] = "THUMBNAIL"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.body shouldBe BODY
        notificationData.title shouldBe TITLE
        notificationData.channelId shouldBe CHANNEL_ID
        notificationData.smallIconResourceId shouldBe SMALL_RESOURCE_ID
        notificationData.colorResourceId shouldBe COLOR_RESOURCE_ID
        notificationData.style shouldBe "THUMBNAIL"
    }

    @Test
    fun testMap_whenTitleIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage(title = null)
        input["notification.channel_id"] = CHANNEL_ID

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.body shouldBe BODY
        notificationData.title shouldBe null
        notificationData.channelId shouldBe CHANNEL_ID
    }

    @Test
    fun testMap_whenMapIsEmpty() {
        val input: MutableMap<String, String> = HashMap()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsAvailable() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["notification.image"] = IMAGE_URL

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.imageUrl shouldNotBe null
    }

    @Test
    fun testMap_whenImageIsNotAvailable() {
        val testImageUrl = "https://fa.il/img.jpg"
        val input: MutableMap<String, String> = createRemoteMessage()
        input["notification.image"] = testImageUrl

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.imageUrl shouldBe testImageUrl
    }

    @Test
    fun testMap_whenNotificationMethodIsSet() {
        val collapseId = "testNotificationId"
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.notification_method.collapse_key"] = collapseId
        input["ems.notification_method.operation"] = "UPDATE"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.collapseId shouldBe collapseId
        notificationData.operation shouldBe NotificationOperation.UPDATE.name
    }

    @Test
    fun testMap_whenNotificationMethodIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.operation shouldBe NotificationOperation.INIT.name
    }

    @Test
    fun testMap_whenNotificationMethodIsSet_withoutCollapseID_shouldReturnWithInitOperation() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.notification_method.operation"] = "UPDATE"

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.operation shouldBe NotificationOperation.INIT.name
    }

    @Test
    fun testMap_notificationData_shouldContain_campaignId() {
        val testCampaignId = "test campaign id"
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.multichannel_id"] = testCampaignId

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.campaignId shouldBe testCampaignId
    }

    @Test
    fun testMap_notificationData_shouldContain_sid() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.sid shouldBe SID
    }

    @Test
    fun testMap_notificationData_shouldContain_actions() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("type", "MECustomEvent")
                .put("id", "Testing")
                .put(
                    "title", JSONObject()
                        .put("en", "Test title")
                )
                .put("name", "test action name")
        )

        val expectedActions =
            """[{"type":"MECustomEvent","id":"Testing","title":{"en":"Test title"},"name":"test action name"}]"""
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.actions"] = testActions.toString()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.actions shouldBe expectedActions
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifDefaultActionTypeIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.tap_actions.default_action.name"] = "test name"
        input["ems.tap_actions.default_action.url"] = "test url"
        input["ems.tap_actions.default_action.payload"] = """{"key":"test payload"}"""

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.defaultAction shouldBe null
    }

    @Test
    fun testMap_notificationData_shouldContain_defaultAction() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.tap_actions.default_action.name"] = "test name"
        input["ems.tap_actions.default_action.type"] = "MECustomEvent"
        input["ems.tap_actions.default_action.url"] = "test url"
        input["ems.tap_actions.default_action.payload"] = """{"key":"test payload"}"""

        val expectedDefaultAction =
            """{"name":"test name","type":"MECustomEvent","url":"test url","payload":{"key":"test payload"}}"""

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.defaultAction shouldBe expectedDefaultAction
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifActionsAreMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.actions shouldBe null
    }

    @Test
    fun testMap_notificationData_shouldContain_inapp() {
        val testInapp = "test inapp"
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.inapp"] = testInapp

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.inapp shouldBe testInapp
    }

    @Test
    fun testMap_notificationData_shouldContain_null_ifInappIsMissing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.inapp shouldBe null
    }

    @Test
    fun testMap_rootParams_shouldContain_rootParams_as_map() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.root_params"] = """{"key1":"value1","key2":"123"}"""

        val expectedRootParams = mapOf(
            "key1" to "value1",
            "key2" to "123"
        )

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.rootParams shouldBe expectedRootParams
    }

    @Test
    fun testMap_rootParams_shouldContain_rootParams_as_map_even_if_the_value_is_a_json() {
        val input: MutableMap<String, String> = createRemoteMessage()
        input["ems.root_params"] = """{"key1":"value1","key2":"123","key3":{"test":"test"}}"""

        val expectedRootParams = mapOf(
            "key1" to "value1",
            "key2" to "123",
            "key3" to """{"test":"test"}"""
        )

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.rootParams shouldBe expectedRootParams
    }

    @Test
    fun testMap_rootParams_shouldContain_empty_map_if_rootParams_are_missing() {
        val input: MutableMap<String, String> = createRemoteMessage()

        val expectedRootParams = mapOf<String, String>()

        val notificationData = remoteMessageMapperV2.map(input)

        notificationData.rootParams shouldBe expectedRootParams
    }


    private fun createRemoteMessage(
        title: String? = TITLE,
        body: String = BODY,
        sid: String = SID
    ): MutableMap<String, String> {
        val payload = mutableMapOf<String, String>()
        title?.let { payload["notification.title"] = it }
        payload["notification.body"] = body
        payload["ems.sid"] = sid

        return payload
    }
}