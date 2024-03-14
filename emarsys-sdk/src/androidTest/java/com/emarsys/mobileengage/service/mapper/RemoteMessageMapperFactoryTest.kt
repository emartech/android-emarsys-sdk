package com.emarsys.mobileengage.service.mapper

import android.content.Context
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock


class RemoteMessageMapperFactoryTest : AnnotationSpec() {
    private lateinit var mockMetaDataReader: MetaDataReader
    private lateinit var context: Context
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var remoteMessageMapperFactory: RemoteMessageMapperFactory

    @Before
    fun init() {
        mockMetaDataReader = mock()
        context = InstrumentationRegistry.getTargetContext()
        mockUuidProvider = mock()

        remoteMessageMapperFactory = RemoteMessageMapperFactory(
            mockMetaDataReader,
            context,
            mockUuidProvider
        )
    }

    @Test
    fun testCreate_shouldReturn_V1Mapper() {
        val testMessageData = mapOf("ems_msg" to "true")
        val mapper = remoteMessageMapperFactory.create(testMessageData)

        mapper::class.java shouldBe RemoteMessageMapperV1::class.java
    }

    @Test
    fun testCreate_shouldReturn_V2Mapper() {
        val testMessageData = mapOf("ems.version" to "testValue")
        val mapper = remoteMessageMapperFactory.create(testMessageData)

        mapper::class.java shouldBe RemoteMessageMapperV2::class.java
    }
}