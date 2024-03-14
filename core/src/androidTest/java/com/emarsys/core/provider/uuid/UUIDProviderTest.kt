package com.emarsys.core.provider.uuid

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldNotBe

class UUIDProviderTest : AnnotationSpec() {
    @Test
    fun testProvideId_returnsNotNullId() {
        val provider = UUIDProvider()
        provider.provideId() shouldNotBe null
    }
}