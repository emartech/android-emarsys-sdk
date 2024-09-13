package com.emarsys.sample.main

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import coil.annotation.ExperimentalCoilApi
import com.emarsys.sample.dashboard.DashboardViewModel
import com.emarsys.sample.main.navigation.BottomNavigationBar
import com.emarsys.sample.main.navigation.NavigationControllerProvider
import com.emarsys.sample.main.sdkinfo.TopExpandableCard
import com.emarsys.sample.ui.theme.AndroidSampleAppTheme

class MainActivity : FragmentActivity() {

    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    @ExperimentalCoilApi
    @ExperimentalMaterialApi
    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dashboardViewModel = DashboardViewModel()
        val mainViewModel = MainViewModel(this.applicationContext, dashboardViewModel)
        val navControllerProvider = NavigationControllerProvider(mainViewModel)

        setContent {
            val navHostController = navControllerProvider.provide()
            AndroidSampleAppTheme {
                Scaffold(modifier = Modifier.imePadding(),
                    topBar = {
                        TopExpandableCard(this.applicationContext, dashboardViewModel)
                    },
                    bottomBar = {
                        BottomNavigationBar(navController = navHostController)
                    },
                    content = {
                        mainViewModel.detailScreen.value.Detail(it)
                    }
                )
            }
        }
    }
}
