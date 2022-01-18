package com.emarsys.mobileengage.iam.jsbridge

import android.content.Intent
import android.net.Uri
import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.model.InAppMessage
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch

@Mockable
class JSCommandFactory(
    private val currentActivityProvider: CurrentActivityProvider,
    private val uiHandler: Handler,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val inAppInternal: InAppInternal,
    private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
    private val onCloseTriggered: OnCloseListener?,
    private val onAppEventTriggered: OnAppEventListener?,
    private val timestampProvider: TimestampProvider
) {

    @Throws(RuntimeException::class)
    fun create(command: CommandType, inAppMessage: InAppMessage? = null): JSCommand {
        return when (command) {
            CommandType.ON_APP_EVENT -> {
                { property, json ->
                    uiHandler.post {
                        onAppEventTriggered?.invoke(property, json)
                    }
                }
            }

            CommandType.ON_CLOSE -> {
                { _, _ ->
                    uiHandler.post {
                        onCloseTriggered?.invoke()
                    }
                }
            }
            CommandType.ON_BUTTON_CLICKED -> {
                { property, _ ->
                    if (inAppMessage != null && property != null) {
                        concurrentHandlerHolder.coreHandler.post {
                            runBlocking {
                                buttonClickedRepository.add(
                                    ButtonClicked(
                                        inAppMessage.campaignId,
                                        property,
                                        timestampProvider.provideTimestamp()
                                    )
                                )
                            }
                            val eventName = "inapp:click"
                            val attributes: MutableMap<String, String?> = mutableMapOf(
                                "campaignId" to inAppMessage.campaignId,
                                "buttonId" to property
                            )

                            if (inAppMessage.sid != null) {
                                attributes["sid"] = inAppMessage.sid
                            }
                            if (inAppMessage.url != null) {
                                attributes["url"] = inAppMessage.url
                            }

                            inAppInternal.trackInternalCustomEvent(eventName, attributes, null)
                        }
                    }
                }
            }
            CommandType.ON_OPEN_EXTERNAL_URL -> {
                @Throws(RuntimeException::class)
                { property, _ ->
                    val activity = currentActivityProvider.get()
                    val link = Uri.parse(property)
                    val intent = Intent(Intent.ACTION_VIEW, link)
                    var success = true
                    if (activity != null) {
                        val latch = CountDownLatch(1)
                        uiHandler.post {
                            try {
                                activity.startActivity(intent)
                            } catch (exception: Exception) {
                                success = false
                            } finally {
                                latch.countDown()
                            }
                        }
                        latch.await()
                        if (!success) {
                            throw Exception("Url cannot be handled by any application!")
                        }
                    } else {
                        throw Exception("UI unavailable!")
                    }
                }
            }
            CommandType.ON_ME_EVENT -> {
                { property, json ->
                    concurrentHandlerHolder.coreHandler.post {
                        val payload = json.optJSONObject("payload")
                        val attributes = payload?.keys()?.asSequence()
                            ?.associateBy({ it }) { payload.getString(it) }
                        inAppInternal.trackCustomEventAsync(property, attributes, null)
                    }
                }
            }
        }
    }

    enum class CommandType {
        ON_APP_EVENT,
        ON_BUTTON_CLICKED,
        ON_CLOSE,
        ON_ME_EVENT,
        ON_OPEN_EXTERNAL_URL
    }
}