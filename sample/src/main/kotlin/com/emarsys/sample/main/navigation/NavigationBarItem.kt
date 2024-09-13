package com.emarsys.sample.main.navigation

import com.emarsys.sample.R

sealed class NavigationBarItem(var route: String, var icon: Int, var title: String) {
    data object BottomDashBoard : NavigationBarItem("dashboard", R.drawable.ic_settings, "Dashboard")
    data object BottomMobileEngage : NavigationBarItem("mobile-engage", R.drawable.mobile_engage_logo_icon, "Mobile Engage")
    data object BottomInbox : NavigationBarItem("inbox", R.drawable.inbox_mailbox_icon, "Inbox")
    data object BottomPredict : NavigationBarItem("predict", R.drawable.predict_scarab_icon, "Predict")
    data object BottomInApp : NavigationBarItem("inapp", R.drawable.mobile_engage_logo_icon, "InApp")
}