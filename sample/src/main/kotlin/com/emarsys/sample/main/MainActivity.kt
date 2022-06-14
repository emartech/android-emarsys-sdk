package com.emarsys.sample.main

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.fragment.app.FragmentActivity
import com.emarsys.sample.ui.component.navbar.BottomNavigationBar
import com.emarsys.sample.main.navigation.NavigationControllerProvider
import com.emarsys.sample.main.sdkinfo.TopExpandableCard
import com.emarsys.sample.ui.theme.AndroidSampleAppTheme

class MainActivity : FragmentActivity() {

    @OptIn(ExperimentalAnimationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class,
        coil.annotation.ExperimentalCoilApi::class
    )
    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application: Application = this.application
        val context: Context = this.applicationContext
        val mainViewModel = MainViewModel(context, application)
        val navControllerProvider = NavigationControllerProvider(mainViewModel)

        setContent {
            val navHostController = navControllerProvider.provide()
            AndroidSampleAppTheme {
                Scaffold(
                    topBar = {
                        TopExpandableCard().TopExpandableCard(context)
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
