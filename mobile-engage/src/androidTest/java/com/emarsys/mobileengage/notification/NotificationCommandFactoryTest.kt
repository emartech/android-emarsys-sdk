package com.emarsys.mobileengage.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.util.JsonUtils
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.notification.command.*
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.tables.row
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class NotificationCommandFactoryTest {
    companion object {
        private const val SID = "129487fw123"
        private const val MISSING_SID = "Missing sid"
        private const val NAME_OF_EVENT = "nameOfTheEvent"
        private const val NAME_OF_MANDATORY_APP_EVENT = "push:payload"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var factory: NotificationCommandFactory
    private lateinit var context: Context
    private lateinit var mockDependencyContainer: MobileEngageDependencyContainer
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockNotificationEventHandlerProvider: EventHandlerProvider
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockEventHandler: EventHandler
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext().applicationContext

        mockEventServiceInternal = mock(EventServiceInternal::class.java)
        mockPushInternal = mock(PushInternal::class.java)
        mockEventHandler = mock(EventHandler::class.java)
        mockNotificationEventHandlerProvider = mock(EventHandlerProvider::class.java).apply {
            whenever(eventHandler).thenReturn(mockEventHandler)
        }
        mockActionCommandFactory = ActionCommandFactory(context, mockEventServiceInternal, mockNotificationEventHandlerProvider)
        mockCurrentActivityProvider = mock(CurrentActivityProvider::class.java).apply {
            whenever(get()).thenReturn(null)
        }

        mockDependencyContainer = FakeMobileEngageDependencyContainer(
                eventServiceInternal = mockEventServiceInternal,
                pushInternal = mockPushInternal,
                notificationEventHandlerProvider = mockNotificationEventHandlerProvider,
                actionCommandFactory = mockActionCommandFactory,
                currentActivityProvider = mockCurrentActivityProvider
        )

        DependencyInjection.setup(mockDependencyContainer)

        factory = NotificationCommandFactory(context)
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenIntentIsEmpty() {
        val command = factory.createNotificationCommand(Intent()) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<HideNotificationShadeCommand>(command) shouldBe true
        contains<TrackMessageOpenCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenTypeIsNotSupported() {
        val command = factory.createNotificationCommand(createUnknownCommandIntent()) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<HideNotificationShadeCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenActionsKeyIsMissing() {
        val command = factory.createNotificationCommand(createIntent(JSONObject(), "actionId")) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
        contains<HideNotificationShadeCommand>(command) shouldBe true
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

        payload.getString("payloadKey") shouldBe "payloadValue"
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
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
        val command = factory.createNotificationCommand(createDefaultOpenExternalLinkIntent("Not valid url!")) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
                "key4" to "value2")

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
        contains<HideNotificationShadeCommand>(command) shouldBe true
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

        command.commands.size shouldBe 5
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<HideNotificationShadeCommand>(command) shouldBe true
        contains<TrackActionClickCommand>(command) shouldBe true
        contains<AppEventCommand>(command) shouldBe true
        contains<NotificationInformationCommand>(command) shouldBe true

    }

    @Test
    fun testCreateNotificationCommand_trackActionCommand_withSids() {
        forall(
                row(SID, extractCommandFromComposite<TrackActionClickCommand>(createAppEventIntent()).sid),
                row(MISSING_SID, extractCommandFromComposite<TrackActionClickCommand>(createAppEventIntent(hasSid = false)).sid),
                row(SID, extractCommandFromComposite<TrackActionClickCommand>(createOpenExternalLinkIntent()).sid),
                row(MISSING_SID, extractCommandFromComposite<TrackActionClickCommand>(createOpenExternalLinkIntent(hasSid = false)).sid),
                row(SID, extractCommandFromComposite<TrackActionClickCommand>(createCustomEventIntent()).sid),
                row(MISSING_SID, extractCommandFromComposite<TrackActionClickCommand>(createCustomEventIntent(hasSid = false)).sid)
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

        payload.getString("payloadKey") shouldBe "payloadValue"
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateMandatoryAppEvent_withCorrectPayload() {
        val intent = createAppEventIntent()
        val commands = extractCommandsFromComposite<AppEventCommand>(intent)
        val payload = commands.first { it.name == NAME_OF_MANDATORY_APP_EVENT }.payload
        val expected = JSONObject(mapOf(
                "customKey" to "customValue",
                "title" to "testTitle",
                "body" to "testBody",
                "u" to mapOf(
                        "sid" to "129487fw123"
                ),
                "ems" to mapOf(
                        "actions" to JSONArray(listOf(JSONObject(mapOf(
                                "type" to "MEAppEvent",
                                "id" to "uniqueActionId",
                                "title" to "title",
                                "name" to NAME_OF_EVENT,
                                "payload" to JSONObject(mapOf("payloadKey" to "payloadValue"))
                        ))))
                ))
        )
        JsonUtils.toFlatMap(payload) shouldBe JsonUtils.toFlatMap(expected)
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
        val emsPayload = """{
                'actions':[
                    {'id':'actionId', 'type': 'MEAppEvent', 'title':'action title', 'name':'eventName'}
                ]
            }"""
        val remoteMessageData = mapOf("ems" to emsPayload)

        val intent = IntentUtils.createNotificationHandlerServiceIntent(
                InstrumentationRegistry.getTargetContext().applicationContext,
                remoteMessageData,
                0,
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
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
        val command = factory.createNotificationCommand(createOpenExternalLinkIntent("Not valid url!")) as CompositeCommand

        contains<LaunchApplicationCommand>(command) shouldBe true
        contains<DismissNotificationCommand>(command) shouldBe true
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
        contains<HideNotificationShadeCommand>(command) shouldBe true
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
                "key4" to "value2")

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

        result[3]::class.java shouldBe LaunchApplicationCommand::class.java
    }

    @Test
    fun testCreateNotificationCommand_shouldNotCreateLaunchCommand_whenInForeground() {
        val mockActivity: Activity = mock()
        whenever(mockCurrentActivityProvider.get()).thenReturn(mockActivity)
        val intent = createOpenExternalLinkIntent()
        val compositeCommands = factory.createNotificationCommand(intent) as CompositeCommand
        val result = compositeCommands.commands

        result.forEach { it::class.java shouldNotBe LaunchApplicationCommand::class.java }
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateNotificationInformationCommand() {

        val intent = createIntent(JSONObject(mapOf("multichannelId" to "campaignId")))

        val command = extractCommandFromComposite<NotificationInformationCommand>(intent)

        command.notificationInformation.campaignId shouldBe "campaignId"
    }

    @Test
    fun testCreateNotificationCommand_shouldNotCreateNotificationInformationCommand_whenMultichannelId_isMissing() {
        val intent = createIntent(JSONObject())

        val command = (factory.createNotificationCommand(intent) as CompositeCommand).commands.filterIsInstance<NotificationInformationCommand>().let { list ->
            list.firstOrNull().takeIf { list.size == 1 }
        }

        command shouldBe null
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Runnable> extractCommandFromComposite(intent: Intent) =
            (factory.createNotificationCommand(intent) as CompositeCommand).commands.filterIsInstance<T>().let { list ->
                list.firstOrNull().takeIf { list.size == 1 }
                        ?: error("CompositeCommand contains multiple commands of type ${T::class.java}: $list")
            }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Runnable> extractCommandsFromComposite(intent: Intent) =
            (factory.createNotificationCommand(intent) as CompositeCommand).commands.filterIsInstance<T>()

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Runnable> contains(command: CompositeCommand) = command.commands.filterIsInstance<T>().isNotEmpty()

    private fun createUnknownCommandIntent(): Intent {
        val unknownType = "NOT_SUPPORTED"
        val json = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId")
                                .put("name", NAME_OF_EVENT)
                                .put("type", unknownType)))
        return createIntent(json, "uniqueActionId", false)
    }

    private fun createDefaultAppEventIntent(hasSid: Boolean = true): Intent {
        val name = NAME_OF_EVENT
        val payload = JSONObject()
                .put("payloadKey", "payloadValue")
        val json = JSONObject()
                .put("default_action", JSONObject()
                        .put("type", "MEAppEvent")
                        .put("name", name)
                        .put("payload", payload))
        return createIntent(json, hasSid = hasSid)
    }

    private fun createDefaultOpenExternalLinkIntent(url: String = "https://emarsys.com", hasSid: Boolean = true): Intent {
        val json = JSONObject()
                .put("default_action", JSONObject()
                        .put("type", "OpenExternalUrl")
                        .put("url", url))
        return createIntent(json, hasSid = hasSid)
    }

    private fun createDefaultCustomEventIntent(eventName: String = "eventName", payload: JSONObject? = null, hasSid: Boolean = true): Intent {
        val action = JSONObject()
                .put("type", "MECustomEvent")
                .put("name", eventName)
        if (payload != null) {
            action.put("payload", payload)
        }

        val json = JSONObject().put("default_action", action)

        return createIntent(json, hasSid = hasSid)
    }

    private fun createAppEventIntent(hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val name = NAME_OF_EVENT
        val payload = JSONObject()
                .put("payloadKey", "payloadValue")
        val json = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("type", "MEAppEvent")
                                .put("id", actionId)
                                .put("title", "title")
                                .put("name", name)
                                .put("payload", payload)))
        return createIntent(json, actionId, hasSid)
    }

    private fun createDismissIntent(hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val json = JSONObject()
                .put("multichannelId", "campaignId")
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("type", "Dismiss")
                                .put("id", actionId)
                                .put("title", "Dismiss")))
        return createIntent(json, actionId, hasSid)
    }

    private fun createOpenExternalLinkIntent(url: String = "https://emarsys.com", hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val json = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("type", "OpenExternalUrl")
                                .put("id", actionId)
                                .put("title", "title")
                                .put("url", url)))
        return createIntent(json, actionId, hasSid)
    }

    private fun createCustomEventIntent(eventName: String = "eventName", payload: JSONObject? = null, hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"

        val action = JSONObject()
                .put("type", "MECustomEvent")
                .put("id", actionId)
                .put("title", "Action button title")
                .put("name", eventName)
        if (payload != null) {
            action.put("payload", payload)
        }

        val json = JSONObject().put("actions", JSONArray().put(action))

        return createIntent(json, actionId, hasSid)
    }

    private fun createIntent(payload: JSONObject, actionId: String? = null, hasSid: Boolean = true): Intent {
        val intent = Intent()

        intent.action = actionId
        val bundle = Bundle()
        bundle.putString("customKey", "customValue")
        bundle.putString("title", "testTitle")
        bundle.putString("body", "testBody")
        bundle.putString("ems", payload.toString())
        if (hasSid) {
            bundle.putString("u", JSONObject().put("sid", SID).toString())
        }
        intent.putExtra("payload", bundle)

        return intent
    }
}