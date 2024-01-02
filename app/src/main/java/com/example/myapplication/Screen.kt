package com.example.myapplication

import androidx.annotation.DrawableRes

sealed class Screen(val route: String, @DrawableRes val icon: Int, val title: String) {
//    object Home : Screen("home", R.drawable.ic_home, "Home")
//    object Search : Screen("search", R.drawable.ic_search, "Search")
//    object Notifications : Screen("notifications", R.drawable.ic_notifications, "Notifications")
//    object Profile : Screen("profile", R.drawable.ic_profile, "Profile")
}
