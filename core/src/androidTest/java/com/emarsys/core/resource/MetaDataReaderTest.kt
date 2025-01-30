package com.emarsys.core.resource

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class MetaDataReaderTest {
    private var reader: MetaDataReader? = null
    private lateinit var mockContext: Context
    private lateinit var applicationInfo: ApplicationInfo

    @Before
    @Throws(Exception::class)
    fun setUp() {
        applicationInfo = ApplicationInfo()
        mockContext = mockk(relaxed = true)
        every {
            mockContext.packageManager.getApplicationInfo(
                any(), any<Int>()
            )
        } returns applicationInfo

        reader = MetaDataReader()
    }

    @Test
    fun testGetIntOrNull_context_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            reader!!.getInt(null, "key")
        }
    }

    @Test
    fun testGetIntOrNull_key_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            reader!!.getInt(getTargetContext(), null)
        }
    }

    @Test
    fun testGetIntOrNull_returnsValue_ifExists() {
        val bundle = Bundle()
        bundle.putInt("something", 42)
        applicationInfo.metaData = bundle

        reader!!.getInt(mockContext, "something").toLong() shouldBe 42
    }

    @Test
    fun intOrNull_shouldReturnNull_ifThereIsNoValue() {
        applicationInfo.metaData = Bundle()

        reader!!.getInt(mockContext, "something").toLong() shouldBe 0
    }

    @Test
    fun testGetInt_context_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            reader!!.getInt(null, "key", 0)
        }
    }

    @Test
    fun testGetInt_key_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            reader!!.getInt(getTargetContext(), null, 0)
        }
    }

    @Test
    fun testGetInt_returnsValue_ifExists() {
        val bundle = Bundle()
        bundle.putInt("something", 43)
        applicationInfo.metaData = bundle

        reader!!.getInt(mockContext, "something", -1).toLong() shouldBe 43
    }

    @Test
    fun int_shouldReturnDefaultValue_ifThereIsNoValue() {
        applicationInfo.metaData = Bundle()
        reader!!.getInt(mockContext, "something").toLong() shouldBe 0
        reader!!.getInt(mockContext, "something", 200).toLong() shouldBe 200
    }
}