package com.emarsys.core.provider.hardwareid

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.test.InstrumentationRegistry
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class HardwareIdProviderTest {

    companion object {
        private const val HARDWARE_ID_KEY = "hardwareId"
        private const val HARDWARE_ID = "hw_value"
    }

    lateinit var context: Context
    lateinit var sharedPrefs: SharedPreferences
    lateinit var sharedPrefsEdit: SharedPreferences.Editor
    lateinit var hardwareIdProvider: HardwareIdProvider

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext().applicationContext

        sharedPrefs = mock(SharedPreferences::class.java)
        sharedPrefsEdit = mock(SharedPreferences.Editor::class.java)
        whenever(sharedPrefsEdit.putString(any(), any())).thenReturn(sharedPrefsEdit)
        whenever(sharedPrefs.edit()).thenReturn(sharedPrefsEdit)

        hardwareIdProvider = HardwareIdProvider(context, sharedPrefs)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_context_shouldNotBeNull() {
        HardwareIdProvider(null, mock(SharedPreferences::class.java))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_sharedPreferences_shouldNotBeNull() {
        HardwareIdProvider(InstrumentationRegistry.getContext(), null)
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromSharedPrefs_ifSetThere() {
        whenever(sharedPrefs.getString(HARDWARE_ID_KEY, null)).thenReturn(HARDWARE_ID)
        val mockContext = mock(Context::class.java)

        hardwareIdProvider = HardwareIdProvider(mockContext, sharedPrefs)

        val hardwareId = hardwareIdProvider.provideHardwareId()

        verify(sharedPrefs).getString(HARDWARE_ID_KEY, null)
        verifyZeroInteractions(mockContext)
        hardwareId shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContext_ifNotFoundInSharedPrefs() {
        val expectedHardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        whenever(sharedPrefs.getString(HARDWARE_ID_KEY, null)).thenReturn(null)

        val actualHardwareId = hardwareIdProvider.provideHardwareId()

        verify(sharedPrefs).getString(HARDWARE_ID_KEY, null)
        actualHardwareId shouldBe expectedHardwareId
    }

    @Test
    fun testProvideHardwareId_shouldSetHardwareId_inSharedPrefs_ifMissing() {
        val expectedHardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        whenever(sharedPrefs.getString(HARDWARE_ID_KEY, null)).thenReturn(null)

        hardwareIdProvider.provideHardwareId()

        Mockito.inOrder(sharedPrefs, sharedPrefsEdit).apply {
            verify(sharedPrefs).getString(HARDWARE_ID_KEY, null)
            verify(sharedPrefs).edit()
            verify(sharedPrefsEdit).putString(HARDWARE_ID_KEY, expectedHardwareId)
            verify(sharedPrefsEdit).commit()
        }

    }

}