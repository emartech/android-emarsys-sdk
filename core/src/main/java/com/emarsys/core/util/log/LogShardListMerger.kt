package com.emarsys.core.util.log

import com.emarsys.core.Mapper
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.Endpoint
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel

class LogShardListMerger(
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UUIDProvider,
    private val deviceInfo: DeviceInfo,
    private val applicationCode: String?,
    private val merchantId: String?
) : Mapper<List<ShardModel>, RequestModel> {

    override fun map(value: List<ShardModel>): RequestModel {
        return if (value.isNotEmpty()) {
            RequestModel.Builder(timestampProvider, uuidProvider)
                .url(Endpoint.LOG_URL)
                .method(RequestMethod.POST)
                .payload(createPayload(value))
                .build()
        } else throw IllegalArgumentException("Shards must not be empty!")
    }

    private fun createPayload(shards: List<ShardModel>): Map<String, Any?> {
        val result: MutableMap<String, Any?> = mutableMapOf()
        val dataList: MutableList<Map<String, Any>> = mutableListOf()
        val deviceInfo = createDeviceInfo()
        shards.forEach { shard ->
            val data: MutableMap<String, Any> = mutableMapOf()
            data["type"] = shard.type
            data["deviceInfo"] = deviceInfo
            data.putAll(shard.data)
            dataList.add(data)
        }

        result["logs"] = dataList
        return result
    }

    private fun createDeviceInfo(): Map<String, String?> {
        val data: MutableMap<String, String?> = mutableMapOf()
        data["platform"] = deviceInfo.platform
        data["appVersion"] = deviceInfo.applicationVersion
        data["sdkVersion"] = deviceInfo.sdkVersion
        data["osVersion"] = deviceInfo.osVersion
        data["model"] = deviceInfo.model
        data["hwId"] = deviceInfo.clientId
        data["isDebugMode"] = deviceInfo.isDebugMode.toString()
        data["applicationCode"] = applicationCode
        data["merchantId"] = merchantId
        return data
    }
}
