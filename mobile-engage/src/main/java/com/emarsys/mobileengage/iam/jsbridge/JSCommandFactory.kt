package com.emarsys.mobileengage.iam.jsbridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.getNullableString
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import java.util.concurrent.CountDownLatch

@Mockable
class JSCommandFactory(
    private val currentActivityProvider: CurrentActivityProvider,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val inAppInternal: InAppInternal,
    private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
    var onCloseTriggered: OnCloseListener?,
    var onAppEventTriggered: OnAppEventListener?,
    private val timestampProvider: TimestampProvider,
    private val clipboardManager: ClipboardManager
) {

    var inAppMetaData: InAppMetaData? = null
    @Throws(RuntimeException::class)
    fun create(command: CommandType): JSCommand {
        return when (command) {
            CommandType.ON_APP_EVENT -> {
                { property, json ->
                    concurrentHandlerHolder.postOnMain {
                        onAppEventTriggered?.invoke(property, json)
                    }
                }
            }

            CommandType.ON_CLOSE -> {
                { _, _ ->
                    concurrentHandlerHolder.postOnMain {
                        onCloseTriggered?.invoke()
                    }
                }
            }
            CommandType.ON_BUTTON_CLICKED -> {
                { property, _ ->
                    if (inAppMetaData != null && property != null) {
                        concurrentHandlerHolder.coreHandler.post {
                            buttonClickedRepository.add(
                                ButtonClicked(
                                    inAppMetaData!!.campaignId,
                                    property,
                                    timestampProvider.provideTimestamp()
                                )
                            )
                            val eventName = "inapp:click"
                            val attributes: MutableMap<String, String> = mutableMapOf(
                                "campaignId" to inAppMetaData!!.campaignId,
                                "buttonId" to property
                            )

                            if (inAppMetaData!!.sid != null) {
                                attributes["sid"] = inAppMetaData!!.sid as String
                            }
                            if (inAppMetaData!!.url != null) {
                                attributes["url"] = inAppMetaData!!.url as String
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
                        concurrentHandlerHolder.postOnMain {
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
                        inAppInternal.trackCustomEventAsync(property!!, attributes, null)
                    }
                }
            }
            CommandType.ON_COPY_TO_CLIPBOARD -> {
                { _, json ->
                    concurrentHandlerHolder.coreHandler.post {
                        val textToCopy = json.getNullableString("text")
                        if(textToCopy != null) {
                            clipboardManager.setPrimaryClip(
                                ClipData.newPlainText("copiedFromInapp", textToCopy)
                            )
                        }
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
        ON_OPEN_EXTERNAL_URL,
        ON_COPY_TO_CLIPBOARD
    }
}