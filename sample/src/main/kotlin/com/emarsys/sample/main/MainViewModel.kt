package com.emarsys.sample.main

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.dashboard.DashboardScreen
import com.emarsys.sample.inapp.InAppScreen
import com.emarsys.sample.inbox.InboxScreen
import com.emarsys.sample.mobileengage.MobileEngageScreen
import com.emarsys.sample.predict.PredictScreen

class MainViewModel(context: Context, application: Application) : ViewModel() {
    val dashBoardScreen = DashboardScreen(context, application)
    val mobileEngageScreen = MobileEngageScreen(context, application)
    val inboxScreen = InboxScreen(context, application)
    val predictScreen = PredictScreen(context, application)
    val inAppScreen = InAppScreen(context, application)

    val detailScreen = mutableStateOf<DetailScreen>(
        dashBoardScreen
    )
}