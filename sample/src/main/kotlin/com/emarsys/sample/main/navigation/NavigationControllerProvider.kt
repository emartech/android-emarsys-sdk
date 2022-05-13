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
        NavHost(navHostController, startDestination = "bottom-dashboard") {
            composable("bottom-dashboard") {  mainViewModel.detailScreen.value = mainViewModel.dashBoardScreen }
            composable("bottom-mobile-engage") { mainViewModel.detailScreen.value = mainViewModel.mobileEngageScreen }
            composable("bottom-inbox") { mainViewModel.detailScreen.value = mainViewModel.inboxScreen }
            composable("bottom-predict") { mainViewModel.detailScreen.value = mainViewModel.predictScreen }
            composable("bottom-inapp") { mainViewModel.detailScreen.value = mainViewModel.inAppScreen }
        }
        return navHostController
    }
}