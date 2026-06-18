package com.emarsys.core.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.shouldBe
import org.junit.Test

class ConnectionWatchDogLiveNetworkTest {

    @Test
    fun isConnected_matchesLiveNetworkValidationState() {
        val context = getTargetContext().applicationContext
        val watchDog = ConnectionWatchDog(context, ConcurrentHandlerHolderFactory.create())

        watchDog.isConnected shouldBe deviceHasValidatedInternet(context)
    }

    private fun deviceHasValidatedInternet(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
