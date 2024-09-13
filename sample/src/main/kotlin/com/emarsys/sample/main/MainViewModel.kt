package com.emarsys.sample.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emarsys.sample.dashboard.DashboardScreen
import com.emarsys.sample.dashboard.DashboardViewModel
import com.emarsys.sample.inapp.InAppScreen
import com.emarsys.sample.inbox.InboxScreen
import com.emarsys.sample.mobileengage.MobileEngageScreen
import com.emarsys.sample.predict.PredictScreen
import com.emarsys.sample.ui.component.screen.DetailScreen

class MainViewModel(
    context: Context,
    dashboardViewModel: DashboardViewModel
) : ViewModel() {
    val dashBoardScreen = DashboardScreen(context, dashboardViewModel)
    val mobileEngageScreen = MobileEngageScreen(context)
    val inboxScreen = InboxScreen(context)
    val predictScreen = PredictScreen(context)
    val inAppScreen = InAppScreen(context)

    val detailScreen = mutableStateOf<DetailScreen>(
        dashBoardScreen
    )
}