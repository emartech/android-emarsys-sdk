package com.emarsys.core.resource

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class MetaDataReaderTest {
    private var reader: MetaDataReader? = null

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
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
        val applicationInfo = ApplicationInfo()
        val bundle = Bundle()
        bundle.putInt("something", 42)
        applicationInfo.metaData = bundle
        val context = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(
            context.packageManager.getApplicationInfo(
                ArgumentMatchers.nullable(
                    String::class.java
                ), ArgumentMatchers.anyInt()
            )
        ).thenReturn(applicationInfo)
        reader!!.getInt(context, "something").toLong() shouldBe 42
    }

    @Test
    fun intOrNull_shouldReturnNull_ifThereIsNoValue() {

        val applicationInfo = ApplicationInfo()
        applicationInfo.metaData = Bundle()
        val context = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(
            context.packageManager.getApplicationInfo(
                ArgumentMatchers.nullable(
                    String::class.java
                ), ArgumentMatchers.anyInt()
            )
        ).thenReturn(applicationInfo)
        reader!!.getInt(context, "something").toLong() shouldBe 0
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
        val applicationInfo = ApplicationInfo()
        val bundle = Bundle()
        bundle.putInt("something", 43)
        applicationInfo.metaData = bundle
        val context = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(
            context.packageManager.getApplicationInfo(
                ArgumentMatchers.nullable(
                    String::class.java
                ), ArgumentMatchers.anyInt()
            )
        ).thenReturn(applicationInfo)
        reader!!.getInt(context, "something", -1).toLong() shouldBe 43
    }

    @Test
    fun int_shouldReturnDefaultValue_ifThereIsNoValue() {
        val applicationInfo = ApplicationInfo()
        applicationInfo.metaData = Bundle()
        val context = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(
            context.packageManager.getApplicationInfo(
                ArgumentMatchers.nullable(
                    String::class.java
                ), ArgumentMatchers.anyInt()
            )
        ).thenReturn(applicationInfo)
        reader!!.getInt(context, "something").toLong() shouldBe 0
        reader!!.getInt(context, "something", 200).toLong() shouldBe 200
    }
}