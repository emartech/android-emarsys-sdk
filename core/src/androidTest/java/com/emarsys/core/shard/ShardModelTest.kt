package com.emarsys.core.shard

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ShardModelTest {

    companion object {
        const val ID = "shard_id"
        const val TYPE = "type"
        const val TIMESTAMP = 123456L
        const val UUID = "uuid"
        const val TTL = java.lang.Long.MAX_VALUE
    }

    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var payload: Map<String, Any>


    @BeforeEach
    fun init() {
        payload = createPayload()
        timestampProvider = mock(TimestampProvider::class.java)
        uuidProvider = mock(UUIDProvider::class.java)
        `when`(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        `when`(uuidProvider.provideId()).thenReturn(UUID)
    }

    @Test
    fun testConstructor_idMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            ShardModel(null, TYPE, mapOf(), 0, 0)
        }
    }

    @Test
    fun testConstructor_typeMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            ShardModel(ID, null, mapOf(), 0, 0)
        }
    }

    @Test
    fun testConstructor_dataMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            ShardModel(ID, TYPE, null, 0, 0)
        }
    }

    @Test
    fun testBuilder_timestampProvider_mustBeNotNull() {
        shouldThrow<IllegalArgumentException> {
            ShardModel.Builder(null, uuidProvider)
        }
    }

    @Test
    fun testBuilder_uuidProvider_mustBeNotNull() {
        shouldThrow<IllegalArgumentException> {
            ShardModel.Builder(timestampProvider, null)
        }
    }

    @Test
    fun testBuilder_type_mustBeSet() {
        shouldThrow<IllegalArgumentException> {
            ShardModel.Builder(timestampProvider, uuidProvider)
                .build()
        }
    }

    @Test
    fun testBuilder_id_shouldBeInitialized_byUUIDProvider() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type("")
            .build()

        shardModel.id shouldBe UUID
    }

    @Test
    fun testBuilder_timestamp_shouldBeInitialized_byTimestampProvider() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type("")
            .build()

        shardModel.timestamp shouldBe TIMESTAMP
    }

    @Test
    fun testBuilder_ttl_shouldHave_defaultValue() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type("")
            .build()

        shardModel.ttl shouldBe TTL
    }

    @Test
    fun testBuilder_data_shouldHave_defaultValue() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type("")
            .build()

        shardModel.data shouldBe mapOf()
    }

    @Test
    fun testBuilder_ttl_shouldBeSet() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type("")
            .ttl(321L)
            .build()

        shardModel.ttl shouldBe 321L
    }

    @Test
    fun testBuilder_type_shouldBeSet() {
        val shardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE)
            .build()

        shardModel.type shouldBe TYPE
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
                        mapOf(
                            "item1" to "item_1",
                            "itemKey2" to 19.9,
                            "itemKey3" to 1
                        ),
                        mapOf(
                            "item2" to "item_2",
                            "itemKey4" to 29.7,
                            "itemKey5" to 3
                        )
                    )
                ),
                "key2" to 234567890.9876543
            )

        shard.data shouldBe expectedPayload
    }

    @Test
    fun testBuilder_with_requiredArguments() {
        val shard: ShardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE)
            .build()

        val expected = ShardModel(UUID, TYPE, mapOf(), TIMESTAMP, TTL)

        shard shouldBe expected
    }

    @Test
    fun testBuilder_with_allArguments() {
        val shard: ShardModel = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE)
            .payloadEntry("key1", payload)
            .payloadEntries(
                mapOf(
                    "key2" to "value2",
                    "key3" to 4.1415
                )
            )
            .ttl(312L)
            .build()

        val expected = ShardModel(
            UUID,
            TYPE,
            mapOf(
                "key1" to payload,
                "key2" to "value2",
                "key3" to 4.1415
            ),
            TIMESTAMP,
            312L
        )

        shard shouldBe expected
    }

    private fun createPayload(): Map<String, Any> {
        return mapOf(
            "key1" to "231213",
            "key2" to listOf(
                mapOf(
                    "item1" to "item_1",
                    "itemKey2" to 19.9,
                    "itemKey3" to 1
                ),
                mapOf(
                    "item2" to "item_2",
                    "itemKey4" to 29.7,
                    "itemKey5" to 3
                )
            )
        )
    }
}