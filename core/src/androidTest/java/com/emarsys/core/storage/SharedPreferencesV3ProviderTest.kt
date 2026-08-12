package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.crypto.SharedPreferenceCrypto
import com.emarsys.core.di.tearDownCoreComponent
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test

class SharedPreferencesV3ProviderTest {

    private lateinit var mockContext: Context
    private lateinit var mockCrypto: SharedPreferenceCrypto
    private lateinit var mockEmarsysEncryptedSharedPreferencesV3: EmarsysEncryptedSharedPreferencesV3

    @Before
    fun setup() {
        mockContext = mockk()
        mockCrypto = mockk()
        mockEmarsysEncryptedSharedPreferencesV3 = mockk(relaxed = true)
        val mockRealSharedPrefs: SharedPreferences = mockk(relaxed = true)
        every { mockContext.getSharedPreferences(any(), any()) } returns mockRealSharedPrefs
    }

    @After
    fun tearDown() {
        tearDownCoreComponent()
    }

    @Test
    fun testProvide() {
        val provider = SharedPreferencesV3Provider(mockContext, "test_file", mockCrypto)
        ReflectionTestUtils.setInstanceField(
            provider,
            "sharedPreferences",
            mockEmarsysEncryptedSharedPreferencesV3
        )

        provider.provide() shouldBe mockEmarsysEncryptedSharedPreferencesV3
    }
}
