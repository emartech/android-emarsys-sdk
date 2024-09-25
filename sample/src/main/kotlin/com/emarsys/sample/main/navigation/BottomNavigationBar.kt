package com.emarsys.sample.main.navigation


import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.emarsys.sample.ui.style.rowWithMaxWidth

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navItems = listOf(
        NavigationBarItem.BottomDashBoard,
        NavigationBarItem.BottomMobileEngage,
        NavigationBarItem.BottomInbox,
        NavigationBarItem.BottomPredict,
        NavigationBarItem.BottomInApp
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        modifier = Modifier.rowWithMaxWidth()
    ) {
        navItems.forEach { navigationButton ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = navigationButton.icon), navigationButton.title) },
                label = {
                    Text(
                        text = navigationButton.title,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                },
                selected = currentRoute == navigationButton.route,
                onClick = {
                    navController.navigate(navigationButton.route)
                }
            )
        }
    }
}



