package com.emarsys.mobileengage.notification


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.util.JsonUtils
import com.emarsys.mobileengage.di.MobileEngageComponent
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.mobileengage.notification.command.CompositeCommand
import com.emarsys.mobileengage.notification.command.CustomEventCommand
import com.emarsys.mobileengage.notification.command.DismissNotificationCommand
import com.emarsys.mobileengage.notification.command.LaunchApplicationCommand
import com.emarsys.mobileengage.notification.command.NotificationInformationCommand
import com.emarsys.mobileengage.notification.command.OpenExternalUrlCommand
import com.emarsys.mobileengage.notification.command.TrackActionClickCommand
import com.emarsys.mobileengage.notification.command.TrackMessageOpenCommand
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.mobileengage.service.NotificationData
import com.emarsys.mobileengage.service.NotificationMethod
import com.emarsys.mobileengage.service.NotificationOperation
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test

class NotificationCommandFactoryTest {
    private companion object {
        const val SID = "129487fw123"
        const val MISSING_SID = "Missing sid"
        const val NAME_OF_EVENT = "nameOfTheEvent"
        const val NAME_OF_MANDATORY_APP_EVENT = "push:payload"
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val COLLAPSE_ID = "testCollapseId"
        const val MULTICHANNEL_ID = "test multiChannel id"
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456
        const val MESSAGE_ID = "messageId"
        const val U = "{\"customField\":\"customValue\"}"
        val notificationMethod = NotificationMethod(COLLAPSE_ID, NotificationOperation.INIT)
        val notificationData = NotificationData(
            null,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_RESOURCE_ID,
            colorResourceId = COLOR_RESOURCE_ID,
            collapseId = COLLAPSE_ID,
            operation = NotificationOperation.INIT.name,
            actions = null,
            defaultAction = null,
            inapp = null,
            rootParams = mapOf(
                "rootParamKey1" to "rootParamValue1",
                "rootParamKey2" to "rootParamValue2"
            ),
            u = U,
            message_id = MESSAGE_ID
        )
    }

    private lateinit var factory: NotificationCommandFactory
    private lateinit var context: Context
    private lateinit var mockConcurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockDependencyContainer: MobileEngageComponent
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockEventHandler: CacheableEventHandler
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext().applicationContext
        mockConcurrentHandlerHolder = mockk(relaxed = true)
        mockEventServiceInternal = mockk(relaxed = true)
        mockPushInternal = mockk(relaxed = true)
        mockEventHandler = mockk(relaxed = true)
        mockActionCommandFactory = ActionCommandFactory(
            context,
            mockEventServiceInternal,
            mockEventHandler,
            mockConcurrentHandlerHolder
        )
        mockCurrentActivityProvider = mockk(relaxed = true)
        every { mockCurrentActivityProvider.get() } returns null

        mockActivity = mockk(relaxed = true)
        every { mockActivity.toString() } returns "com.emarsys.NotificationOpenedActivity"


        mockDependencyContainer = FakeMobileEngageDependencyContainer(
            eventServiceInternal = mockEventServiceInternal,
            pushInternal = mockPushInternal,
            notificationCacheableEventHandler = mockEventHandler,
            notificationActionCommandFactory = mockActionCommandFactory,
            currentActivityProvider = mockCurrentActivityProvider
        )

        setupMobileEngageComponent(mockDependencyContainer)

        factory = NotificationCommandFactory(context)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenIntentIsEmpty() {
        val command = factory.createNotificationCommand(Intent()) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenTypeIsNotSupported() {
        val command =
            factory.createNotificationCommand(createUnknownCommandIntent()) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenCurrentActivityIsNull() {
        val command = factory.createNotificationCommand(createAppEventIntent()) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenCurrentActivityIsNotificationOpenedActivity() {
        every { mockCurrentActivityProvider.get() } returns mockActivity
        val command = factory.createNotificationCommand(createAppEventIntent()) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenActionsKeyIsMissing() {
        val command = factory.createNotificationCommand(
            createIntent(
                notificationData,
                "actionId"
            )
        ) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateAppEvent_asPartOfACompositeCommand() {
        val intent = createDefaultAppEventIntent()
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
        command.commands.filterIsInstance<AppEventCommand>().size shouldBe 2
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateAppEvent_withCorrectName() {
        val intent = createDefaultAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val command = commands.first { it.name == NAME_OF_EVENT }
        command shouldNotBe null
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateAppEvent_withCorrectPayload() {
        val intent = createDefaultAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val payload = commands.first { it.name == NAME_OF_EVENT }.payload

        payload?.getString("payloadKey") shouldBe "payloadValue"
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateAppEvent_withCorrectNotificationEventHandler() {
        val intent = createDefaultAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val handler = commands.first { it.name == NAME_OF_EVENT }.notificationEventHandler

        handler shouldBe mockEventHandler
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateOpenExternalLinkCommand_asPartOfACompositeCommand() {
        val intent = createDefaultOpenExternalLinkIntent("https://www.emarsys.com")
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
        contains<OpenExternalUrlCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateOpenExternalLinkCommand_withCorrectParameters() {
        val intent = createDefaultOpenExternalLinkIntent("https://www.emarsys.com")
        val command = extractCommandFromComposite<OpenExternalUrlCommand>(intent)

        command.context shouldBe context
        command.intent.data shouldBe Uri.parse("https://www.emarsys.com")
        command.intent.action shouldBe Intent.ACTION_VIEW

        val flags = command.intent.flags
        flags and Intent.FLAG_ACTIVITY_NEW_TASK shouldBe Intent.FLAG_ACTIVITY_NEW_TASK
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateAppLaunchCommand_insteadOf_OpenExternalLinkCommand_whenCannotResolveUrl() {
        val command =
            factory.createNotificationCommand(createDefaultOpenExternalLinkIntent("Not valid url!")) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_defaultAction_shouldCreateCustomEventCommand_asPartOfACompositeCommand() {
        val intent = createDefaultCustomEventIntent("eventName")
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand
        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
        contains<CustomEventCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_default_shouldCreateCustomEventCommand_withCorrectEventName() {
        val eventName = "eventName"

        val intent = createDefaultCustomEventIntent(eventName)

        val command = extractCommandFromComposite<CustomEventCommand>(intent)

        command.eventName shouldBe eventName
    }

    @Test
    fun testCreateNotificationCommand_default_shouldCreateCustomEventCommand_withCorrectEventAttributes() {
        val eventName = "eventName"

        val payload = JSONObject()
            .put("key1", true)
            .put("key2", 3.14)
            .put("key3", JSONObject().put("key5", "value1"))
            .put("key4", "value2")

        val attributes = mapOf(
            "key1" to "true",
            "key2" to "3.14",
            "key3" to """{"key5":"value1"}""",
            "key4" to "value2"
        )

        val intent = createDefaultCustomEventIntent(eventName, payload)

        val command = extractCommandFromComposite<CustomEventCommand>(intent)

        command.eventAttributes shouldBe attributes
    }

    @Test
    fun testCreateNotificationCommand_default_shouldCreateCustomEventCommand_withoutEventAttributes() {
        val eventName = "eventName"

        val intent = createDefaultCustomEventIntent(eventName)

        val command = extractCommandFromComposite<CustomEventCommand>(intent)

        command.eventAttributes shouldBe null
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_asPartOfACompositeCommand() {
        val intent = createAppEventIntent()
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
        command.commands.filterIsInstance<AppEventCommand>().size shouldBe 2
    }

    @Test
    fun testCreateNotificationCommand_shouldDismiss_asPartOfACompositeCommand() {
        val intent = createDismissIntent()
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        command.commands.size shouldBe 4
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
        contains<NotificationInformationCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_trackActionCommand_withSids() = runBlocking {
        forAll(
            row(
                SID,
                extractCommandFromComposite<TrackActionClickCommand>(createAppEventIntent()).sid
            ),
            row(
                MISSING_SID,
                extractCommandFromComposite<TrackActionClickCommand>(createAppEventIntent(hasSid = false)).sid
            ),
            row(
                SID,
                extractCommandFromComposite<TrackActionClickCommand>(createOpenExternalLinkIntent()).sid
            ),
            row(
                MISSING_SID,
                extractCommandFromComposite<TrackActionClickCommand>(
                    createOpenExternalLinkIntent(hasSid = false)
                ).sid
            ),
            row(
                SID,
                extractCommandFromComposite<TrackActionClickCommand>(createCustomEventIntent()).sid
            ),
            row(
                MISSING_SID,
                extractCommandFromComposite<TrackActionClickCommand>(createCustomEventIntent(hasSid = false)).sid
            )
        ) { expectedSid, actualSid -> actualSid shouldBe expectedSid }
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_withCorrectName() {
        val intent = createAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val command = commands.first { it.name == NAME_OF_EVENT }

        command shouldNotBe null
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_withCorrectPayload() {
        val intent = createAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val payload = commands.first { it.name == NAME_OF_EVENT }.payload

        payload?.getString("payloadKey") shouldBe "payloadValue"
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateMandatoryAppEvent_withCorrectPayload() {
        val intent = createAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val payload = commands.first { it.name == NAME_OF_MANDATORY_APP_EVENT }.payload
        val expected = JSONObject(
            mapOf(
                "rootParamKey1" to "rootParamValue1",
                "rootParamKey2" to "rootParamValue2",
                "u" to U,
                "title" to TITLE,
                "body" to BODY,
                "channelId" to CHANNEL_ID,
                "campaignId" to MULTICHANNEL_ID,
                "sid" to SID,
                "smallIconResourceId" to SMALL_RESOURCE_ID,
                "colorResourceId" to COLOR_RESOURCE_ID,
                "collapseId" to COLLAPSE_ID,
                "operation" to notificationMethod.operation.name,
                "actions" to JSONArray(
                    listOf(
                        JSONObject(
                            mapOf(
                                "type" to "MEAppEvent",
                                "id" to "uniqueActionId",
                                "title" to "title",
                                "name" to NAME_OF_EVENT,
                                "payload" to JSONObject(mapOf("payloadKey" to "payloadValue"))
                            )
                        )
                    )
                ),
                "u" to U,
                "message_id" to MESSAGE_ID
            )
        )
        JsonUtils.toFlatMap(payload!!) shouldBe JsonUtils.toFlatMap(expected)
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_withCorrectNotificationEventHandler() {
        val intent = createAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val handler = commands.first { it.name == NAME_OF_EVENT }.notificationEventHandler

        handler shouldBe mockEventHandler
    }

    @Test
    fun testCreateNotificationCommand_appEvent_worksWithIntentUtils() {
        val actions = """[
                    {'id':'actionId', 'type': 'MEAppEvent', 'title':'action title', 'name':'eventName'}
                ]
            """
        val intent = IntentUtils.createNotificationHandlerServiceIntent(
            InstrumentationRegistry.getTargetContext().applicationContext,
            notificationData.copy(actions = actions),
            "actionId"
        )

        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val command = commands.first { it.name == "eventName" }
        command shouldNotBe null
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateOpenExternalLinkCommand_asPartOfACompositeCommand() {
        val intent = createOpenExternalLinkIntent("https://www.emarsys.com")
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
        contains<OpenExternalUrlCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true

    }

    @Test
    fun testCreateNotificationCommand_shouldCreateOpenExternalLinkCommand_withCorrectParameters() {
        val intent = createOpenExternalLinkIntent("https://www.emarsys.com")
        val command = extractCommandFromComposite<OpenExternalUrlCommand>(intent)

        command.context shouldBe context
        command.intent.data shouldBe Uri.parse("https://www.emarsys.com")
        command.intent.action shouldBe Intent.ACTION_VIEW

        val flags = command.intent.flags
        flags and Intent.FLAG_ACTIVITY_NEW_TASK shouldBe Intent.FLAG_ACTIVITY_NEW_TASK
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_insteadOf_OpenExternalLinkCommand_whenCannotResolveUrl() {
        val command =
            factory.createNotificationCommand(createOpenExternalLinkIntent("Not valid url!")) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_asPartOfACompositeCommand() {
        val intent = createCustomEventIntent("eventName")
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
        contains<CustomEventCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true

    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_withCorrectEventName() {
        val eventName = "eventName"

        val intent = createCustomEventIntent(eventName)

        val command = extractCommandFromComposite<CustomEventCommand>(intent)

        command.eventName shouldBe eventName
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_withCorrectEventAttributes() {
        val eventName = "eventName"

        val payload = JSONObject()
            .put("key1", true)
            .put("key2", 3.14)
            .put("key3", JSONObject().put("key5", "value1"))
            .put("key4", "value2")

        val attributes = mapOf(
            "key1" to "true",
            "key2" to "3.14",
            "key3" to """{"key5":"value1"}""",
            "key4" to "value2"
        )

        val intent = createCustomEventIntent(eventName, payload)

        val command = extractCommandFromComposite<CustomEventCommand>(intent)

        command.eventAttributes shouldBe attributes
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_withoutEventAttributes() {
        val eventName = "eventName"

        val intent = createCustomEventIntent(eventName)

        val command = extractCommandFromComposite<CustomEventCommand>(intent)

        command.eventAttributes shouldBe null
    }

    @Test
    fun testCreateNotificationCommand_launchCommandShouldBeThirdCommand() {
        val intent = createOpenExternalLinkIntent()
        val compositeCommands = factory.createNotificationCommand(intent) as CompositeCommand
        val result = compositeCommands.commands

        result[2]::class.java shouldBe LaunchApplicationCommand::class.java
    }

    @Test
    fun testCreateNotificationCommand_shouldNotCreateLaunchCommand_whenInForeground() {
        val mockActivity: Activity = mockk(relaxed = true)
        every { mockCurrentActivityProvider.get() } returns mockActivity
        val intent = createOpenExternalLinkIntent()
        val compositeCommands = factory.createNotificationCommand(intent) as CompositeCommand
        val result = compositeCommands.commands

        result.forEach {
            it::class.java shouldNotBe LaunchApplicationCommand::class.java
        }
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateNotificationInformationCommand() {

        val intent = createIntent(notificationData)

        val command = extractCommandFromComposite<NotificationInformationCommand>(intent)

        command.notificationInformation.campaignId shouldBe MULTICHANNEL_ID
    }

    @Test
    fun testCreateNotificationCommand_shouldNotCreateNotificationInformationCommand_whenMultichannelId_isMissing() {
        val intent = createIntent(notificationData.copy(campaignId = null))
        val command =
            (factory.createNotificationCommand(intent) as CompositeCommand).commands.filterIsInstance<NotificationInformationCommand>()
                .let { list ->
                    list.firstOrNull().takeIf { list.size == 1 }
                }

        command shouldBe null
    }

    private inline fun <reified T : Runnable> extractCommandFromComposite(intent: Intent) =
        (factory.createNotificationCommand(intent) as CompositeCommand).commands.filterIsInstance<T>()
            .let { list ->
                list.firstOrNull().takeIf { list.size == 1 }
                    ?: error(
                        "CompositeCommand contains multiple commands of type ${
                            T::
                            class.java
                        }: $list"
                    )
            }

    private inline fun <reified T : Runnable> extractCommandsFromComposite(intent: Intent) =
        (factory.createNotificationCommand(intent) as CompositeCommand).commands.filterIsInstance<T>()

    private inline fun <reified T : Runnable> contains(command: CompositeCommand) =
        command.commands.filterIsInstance<T>().isNotEmpty()

    private fun createUnknownCommandIntent(): Intent {
        val unknownType = "NOT_SUPPORTED"
        val actions = JSONArray()
            .put(
                JSONObject()
                    .put("id", "uniqueActionId")
                    .put("name", NAME_OF_EVENT)
                    .put("type", unknownType)
            )

        return createIntent(
            notificationData.copy(actions = actions.toString()),
            "uniqueActionId",
            false
        )
    }

    private fun createDefaultAppEventIntent(hasSid: Boolean = true): Intent {
        val name = NAME_OF_EVENT
        val payload = JSONObject()
            .put("payloadKey", "payloadValue")
        val defaultAction = JSONObject()
            .put("type", "MEAppEvent")
            .put("name", name)
            .put("payload", payload)


        return createIntent(
            notificationData.copy(defaultAction = defaultAction.toString()),
            hasSid = hasSid
        )
    }

    private fun createDefaultOpenExternalLinkIntent(
        url: String = "https://emarsys.com",
        hasSid: Boolean = true
    ): Intent {
        val defaultAction = JSONObject()
            .put("type", "OpenExternalUrl")
            .put("url", url)


        return createIntent(
            notificationData.copy(defaultAction = defaultAction.toString()),
            hasSid = hasSid
        )
    }

    private fun createDefaultCustomEventIntent(
        eventName: String = "eventName",
        payload: JSONObject? = null,
        hasSid: Boolean = true
    ): Intent {
        val defaultAction = JSONObject()
            .put("type", "MECustomEvent")
            .put("name", eventName)
        if (payload != null) {
            defaultAction.put("payload", payload)
        }

        return createIntent(
            notificationData.copy(defaultAction = defaultAction.toString()),
            hasSid = hasSid
        )
    }

    private fun createAppEventIntent(hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val name = NAME_OF_EVENT
        val payload = JSONObject()
            .put("payloadKey", "payloadValue")

        val actions = JSONArray()
            .put(
                JSONObject()
                    .put("type", "MEAppEvent")
                    .put("id", actionId)
                    .put("title", "title")
                    .put("name", name)
                    .put("payload", payload)
            )

        return createIntent(notificationData.copy(actions = actions.toString()), actionId, hasSid)
    }

    private fun createDismissIntent(hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val actions = JSONArray()
            .put(
                JSONObject()
                    .put("type", "Dismiss")
                    .put("id", actionId)
                    .put("title", "Dismiss")
            )

        return createIntent(notificationData.copy(actions = actions.toString()), actionId, hasSid)
    }

    private fun createOpenExternalLinkIntent(
        url: String = "https://emarsys.com",
        hasSid: Boolean = true
    ): Intent {
        val actionId = "uniqueActionId"
        val actions = JSONArray().put(
            JSONObject()
                .put("type", "OpenExternalUrl")
                .put("id", actionId)
                .put("title", "title")
                .put("url", url)
        )

        return createIntent(notificationData.copy(actions = actions.toString()), actionId, hasSid)
    }

    private fun createCustomEventIntent(
        eventName: String = "eventName",
        payload: JSONObject? = null,
        hasSid: Boolean = true
    ): Intent {
        val actionId = "uniqueActionId"

        val action = JSONObject()
            .put("type", "MECustomEvent")
            .put("id", actionId)
            .put("title", "Action button title")
            .put("name", eventName)
        if (payload != null) {
            action.put("payload", payload)
        }

        val actions = JSONArray().put(action)

        return createIntent(notificationData.copy(actions = actions.toString()), actionId, hasSid)
    }

    private fun createIntent(
        payload: NotificationData,
        actionId: String? = null,
        hasSid: Boolean = true
    ): Intent {
        val intent = Intent()

        intent.action = actionId
        val notificationDataWithSidIfNeeded = if (hasSid) {
            payload
        } else payload.copy(sid = "Missing sid")
        intent.putExtra("payload", notificationDataWithSidIfNeeded)

        return intent
    }
}