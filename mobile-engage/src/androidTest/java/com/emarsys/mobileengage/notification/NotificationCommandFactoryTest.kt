package com.emarsys.mobileengage.notification

import android.support.test.InstrumentationRegistry
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.api.NotificationEventHandler
import com.emarsys.mobileengage.notification.command.*
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.tables.row
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class NotificationCommandFactoryTest {
    companion object {
        private const val SID = "129487fw123"
        private const val MISSING_SID = "Missing sid"
    }

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var factory: NotificationCommandFactory
    private lateinit var context: Context
    private lateinit var mobileEngageInternal: MobileEngageInternal
    private lateinit var notificationEventHandler: NotificationEventHandler

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext()
        mobileEngageInternal = mock(MobileEngageInternal::class.java)
        notificationEventHandler = mock(NotificationEventHandler::class.java)
        factory = NotificationCommandFactory(context, mobileEngageInternal, notificationEventHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_context_shouldNotBeNull() {
        NotificationCommandFactory(null, mobileEngageInternal, notificationEventHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_shouldNotBeNull() {
        NotificationCommandFactory(context, null, notificationEventHandler)
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenIntentIsEmpty() {
        val command = factory.createNotificationCommand(Intent())

        command::class.java shouldBe LaunchApplicationCommand::class.java
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenTypeIsNotSupported() {
        val command = factory.createNotificationCommand(createUnknownCommandIntent())

        command::class.java shouldBe LaunchApplicationCommand::class.java
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenActionsKeyIsMissing() {
        val command = factory.createNotificationCommand(createIntent("actionId", JSONObject()))

        command::class.java shouldBe LaunchApplicationCommand::class.java
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_asPartOfACompositeCommand() {
        val intent = createAppEventIntent()
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        command.commands.map { it::class.java } shouldBe listOf(
                TrackActionClickCommand::class.java,
                HideNotificationShadeCommand::class.java,
                AppEventCommand::class.java)
    }

    @Test
    fun testCreateNotificationCommand_trackActionCommand_withSids() {
        forall(
                row(SID, extractCommandFromComposite<TrackActionClickCommand>(createAppEventIntent(), 0).sid),
                row(MISSING_SID, extractCommandFromComposite<TrackActionClickCommand>(createAppEventIntent(hasSid = false), 0).sid),
                row(SID, extractCommandFromComposite<TrackActionClickCommand>(createOpenExternalLinkIntent(), 0).sid),
                row(MISSING_SID, extractCommandFromComposite<TrackActionClickCommand>(createOpenExternalLinkIntent(hasSid = false), 0).sid),
                row(SID, extractCommandFromComposite<TrackActionClickCommand>(createCustomEventIntent(), 0).sid),
                row(MISSING_SID, extractCommandFromComposite<TrackActionClickCommand>(createCustomEventIntent(hasSid = false), 0).sid)
        ) { expectedSid, actualSid -> actualSid shouldBe expectedSid }
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_withCorrectName() {
        val intent = createAppEventIntent()
        val command = extractCommandFromComposite<AppEventCommand>(intent, 2)

        command.name shouldBe "nameOfTheEvent"
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_withCorrectPayload() {
        val intent = createAppEventIntent()
        val command = extractCommandFromComposite<AppEventCommand>(intent, 2)

        val payload = command.payload

        payload.getString("payloadKey") shouldBe "payloadValue"
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppEvent_withCorrectNotificationEventHandler() {
        val intent = createAppEventIntent()
        val command = extractCommandFromComposite<AppEventCommand>(intent, 2)

        val handler = command.notificationEventHandler

        handler shouldBe notificationEventHandler
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
                InstrumentationRegistry.getTargetContext().getApplicationContext(),
                remoteMessageData,
                0,
                "actionId"
        )

        val command = extractCommandFromComposite<AppEventCommand>(intent, 2)

        command.name shouldBe "eventName"
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateOpenExternalLinkCommand_asPartOfACompositeCommand() {
        val intent = createOpenExternalLinkIntent("https://www.emarsys.com")
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        command.commands.map { it::class.java } shouldBe listOf(
                TrackActionClickCommand::class.java,
                HideNotificationShadeCommand::class.java,
                OpenExternalUrlCommand::class.java)
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateOpenExternalLinkCommand_withCorrectParameters() {
        val intent = createOpenExternalLinkIntent("https://www.emarsys.com")
        val command = extractCommandFromComposite<OpenExternalUrlCommand>(intent, 2)

        command.context shouldBe context
        command.intent.data shouldBe Uri.parse("https://www.emarsys.com")
        command.intent.action shouldBe Intent.ACTION_VIEW

        val flags = command.intent.flags
        flags and Intent.FLAG_ACTIVITY_NEW_TASK shouldBe Intent.FLAG_ACTIVITY_NEW_TASK
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateAppLaunchCommand_insteadOf_OpenExternalLinkCommand_whenCantResolveUrl() {
        val command = factory.createNotificationCommand(createOpenExternalLinkIntent("Not valid url!"))

        command::class.java shouldBe LaunchApplicationCommand::class.java
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_asPartOfACompositeCommand() {
        val intent = createCustomEventIntent("eventName")
        val command = factory.createNotificationCommand(intent)

        command shouldNotBe null
        command::class.java shouldBe CompositeCommand::class.java

        command as CompositeCommand

        command.commands.map { it::class.java } shouldBe listOf(
                TrackActionClickCommand::class.java,
                HideNotificationShadeCommand::class.java,
                CustomEventCommand::class.java)
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_withCorrectEventName() {
        val eventName = "eventName"

        val intent = createCustomEventIntent(eventName)

        val command = extractCommandFromComposite<CustomEventCommand>(intent, 2)

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

        val command = extractCommandFromComposite<CustomEventCommand>(intent, 2)

        command.eventAttributes shouldBe attributes
    }

    @Test
    fun testCreateNotificationCommand_shouldCreateCustomEventCommand_withoutEventAttributes() {
        val eventName = "eventName"

        val intent = createCustomEventIntent(eventName)

        val command = extractCommandFromComposite<CustomEventCommand>(intent, 2)

        command.eventAttributes shouldBe null
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Runnable> extractCommandFromComposite(intent: Intent, index: Int) =
            (factory.createNotificationCommand(intent) as CompositeCommand).commands[index] as T

    private fun createUnknownCommandIntent(): Intent {
        val unknownType = "NOT_SUPPORTED"
        val json = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId")
                                .put("name", "nameOfTheEvent")
                                .put("type", unknownType)))
        return createIntent("uniqueActionId", json, false)
    }

    private fun createAppEventIntent(hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val name = "nameOfTheEvent"
        val payload = JSONObject()
                .put("payloadKey", "payloadValue")
        val json = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", actionId)
                                .put("title", "title")
                                .put("name", name)
                                .put("payload", payload)
                                .put("type", "MEAppEvent")))
        return createIntent(actionId, json, hasSid)
    }

    private fun createOpenExternalLinkIntent(url: String = "https://emarsys.com", hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"
        val json = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", actionId)
                                .put("title", "title")
                                .put("url", url)
                                .put("type", "OpenExternalUrl")))
        return createIntent(actionId, json, hasSid)
    }

    private fun createCustomEventIntent(eventName: String = "eventName", payload: JSONObject? = null, hasSid: Boolean = true): Intent {
        val actionId = "uniqueActionId"

        val action = JSONObject()
                .put("id", actionId)
                .put("title", "Action button title")
                .put("type", "MECustomEvent")
                .put("name", eventName)
        if (payload != null) {
            action.put("payload", payload)
        }

        val json = JSONObject().put("actions", JSONArray().put(action))

        return createIntent(actionId, json, hasSid)
    }

    private fun createIntent(actionId: String, payload: JSONObject, hasSid: Boolean = true): Intent {
        val intent = Intent()

        intent.action = actionId

        val bundle = Bundle()
        bundle.putString("ems", payload.toString())
        if (hasSid) {
            bundle.putString("u", JSONObject().put("sid", SID).toString())
        }
        intent.putExtra("payload", bundle)

        return intent
    }
}