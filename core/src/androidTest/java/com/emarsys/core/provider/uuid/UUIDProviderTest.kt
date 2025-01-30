package com.emarsys.core.provider.uuid

import io.kotest.matchers.shouldNotBe
import org.junit.Test

class UUIDProviderTest  {
    @Test
    fun testProvideId_returnsNotNullId() {
        val provider = UUIDProvider()
        provider.provideId() shouldNotBe null
    }
}