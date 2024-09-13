package com.emarsys.sample.main.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emarsys.sample.main.MainViewModel

class NavigationControllerProvider(private val mainViewModel: MainViewModel) {

    @Composable
    fun provide(): NavHostController {
        val navHostController = rememberNavController()
        NavHost(navHostController, startDestination = "dashboard") {
            composable("dashboard") {  mainViewModel.detailScreen.value = mainViewModel.dashBoardScreen }
            composable("mobile-engage") { mainViewModel.detailScreen.value = mainViewModel.mobileEngageScreen }
            composable("inbox") { mainViewModel.detailScreen.value = mainViewModel.inboxScreen }
            composable("predict") { mainViewModel.detailScreen.value = mainViewModel.predictScreen }
            composable("inapp") { mainViewModel.detailScreen.value = mainViewModel.inAppScreen }
        }
        return navHostController
    }
}