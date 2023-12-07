package com.emarsys.mobileengage.service.mapper

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.FileDownloader
import com.emarsys.mobileengage.service.MessagingServiceUtils.MESSAGE_FILTER
import com.emarsys.mobileengage.service.RemoteMessageMapper
import com.emarsys.mobileengage.service.RemoteMessageMapperV1
import com.emarsys.mobileengage.service.RemoteMessageMapperV2

@Mockable
class RemoteMessageMapperFactory(
    private val metaDataReader: MetaDataReader,
    private val context: Context,
    private val fileDownloader: FileDownloader,
    private val deviceInfo: DeviceInfo,
    private val uuidProvider: UUIDProvider
) {

    fun create(remoteMessageData: Map<String, String>): RemoteMessageMapper {
        return if (remoteMessageData.containsKey(MESSAGE_FILTER)) {
            RemoteMessageMapperV1(
                metaDataReader,
                context,
                fileDownloader,
                deviceInfo,
                uuidProvider
            )
        } else {
            RemoteMessageMapperV2(
                metaDataReader,
                context,
                fileDownloader,
                deviceInfo,
                uuidProvider
            )
        }
    }
}