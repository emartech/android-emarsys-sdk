package com.emarsys.core.shard;

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ShardModelTest {

    companion object {
        const val ID = "shard_id"
        const val TYPE = "type"
        const val TIMESTAMP = 123456L
        const val UUID = "uuid"
        const val TTL = java.lang.Long.MAX_VALUE
    }

    lateinit var timestampProvider: TimestampProvider
    lateinit var uuidProvider: UUIDProvider
    lateinit var payload: Map<String, Any>

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        payload = createPayload()
        timestampProvider = mock(TimestampProvider::class.java)
        uuidProvider = mock(UUIDProvider::class.java)
        `when`(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        `when`(uuidProvider.provideId()).thenReturn(UUID)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_idMustNotBeNull() {
        ShardModel(null, TYPE, mapOf(), 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_typeMustNotBeNull() {
        ShardModel(ID, null, mapOf(), 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_dataMustNotBeNull() {
        ShardModel(ID, TYPE, null, 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilder_timestampProvider_mustBeNotNull() {
        ShardModel.Builder(null, uuidProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilder_uuidProvider_mustBeNotNull() {
        ShardModel.Builder(timestampProvider, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilder_type_mustBeSet() {
        ShardModel.Builder(timestampProvider, uuidProvider)
                .build()
    }

    @Test
    fun testBuilder_id_shouldBeInitialized_byUUIDProvider() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type("")
                .build()

        assertEquals(UUID, shardModel.id)
    }

    @Test
    fun testBuilder_timestamp_shouldBeInitialized_byTimestampProvider() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type("")
                .build()

        assertEquals(TIMESTAMP, shardModel.timestamp)
    }

    @Test
    fun testBuilder_ttl_shouldHave_defaultValue() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type("")
                .build()

        assertEquals(TTL, shardModel.ttl)
    }

    @Test
    fun testBuilder_data_shouldHave_defaultValue() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type("")
                .build()

        assertEquals(mapOf<String, Any>(), shardModel.data)
    }

    @Test
    fun testBuilder_ttl_shouldBeSet() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type("")
                .ttl(321L)
                .build()

        assertEquals(321L, shardModel.ttl)
    }

    @Test
    fun testBuilder_type_shouldBeSet() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE)
                .build()

        assertEquals(TYPE, shardModel.type)
    }

    @Test
    fun testBuilder_shouldConcatenate_complexPayload_fromTypeAndData() {
        val shard: ShardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE)
                .payloadEntry("key", createPayload())
                .payloadEntry("key2", 234567890.9876543)
                .build()

        val expectedPayload =
                mapOf(
                        "key" to mapOf(
                                "key1" to "231213",
                                "key2" to listOf(
                                        mapOf("item1" to "item_1",
                                                "itemKey2" to 19.9,
                                                "itemKey3" to 1),
                                        mapOf("item2" to "item_2",
                                                "itemKey4" to 29.7,
                                                "itemKey5" to 3)
                                )
                        ),
                        "key2" to 234567890.9876543)

        assertEquals(expectedPayload, shard.data)
    }

    @Test
    fun testBuilder_with_requiredArguments() {
        val shard: ShardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE)
                .build()

        val expected = ShardModel(UUID, TYPE, mapOf(), TIMESTAMP, TTL)

        assertEquals(expected, shard)
    }

    @Test
    fun testBuilder_with_allArguments() {
        val shard: ShardModel = ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE)
                .payloadEntry("key1", payload)
                .payloadEntries(mapOf(
                        "key2" to "value2",
                        "key3" to 4.1415
                ))
                .ttl(312L)
                .build()

        val expected = ShardModel(
                UUID,
                TYPE,
                mapOf(
                        "key1" to payload,
                        "key2" to "value2",
                        "key3" to 4.1415),
                TIMESTAMP,
                312L)

        assertEquals(expected, shard)
    }

    private fun createPayload(): Map<String, Any> {
        return mapOf(
                "key1" to "231213",
                "key2" to listOf(
                        mapOf("item1" to "item_1",
                                "itemKey2" to 19.9,
                                "itemKey3" to 1),
                        mapOf("item2" to "item_2",
                                "itemKey4" to 29.7,
                                "itemKey5" to 3))
        )
    }
}