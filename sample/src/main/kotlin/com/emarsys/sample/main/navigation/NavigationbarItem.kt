package com.emarsys.sample.main.navigation

import com.emarsys.sample.R

sealed class NavigationbarItem(var route: String, var icon: Int, var title: String) {
    object BottomDashBoard : NavigationbarItem("bottom-dashboard", R.drawable.ic_settings, "Dashboard")
    object BottomMobileEngage : NavigationbarItem("bottom-mobile-engage", R.drawable.mobile_engage_logo_icon, "Mobile Engage")
    object BottomInbox : NavigationbarItem("bottom-inbox", R.drawable.inbox_mailbox_icon, "Inbox")
    object BottomPredict : NavigationbarItem("bottom-predict", R.drawable.predict_scarab_icon, "Predict")
    object BottomInApp : NavigationbarItem("bottom-inapp", R.drawable.mobile_engage_logo_icon, "InApp")
}