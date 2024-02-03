package com.company.test

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen (
    val route: String,
    val title: String,
    val icon: ImageVector
){
    object Create : BottomBarScreen(
        route = "create",
        title = "Create",
        icon = Icons.Default.Create
    )
    object Gallery : BottomBarScreen(
        route = "gallery",
        title = "Gallery",
        icon = Icons.Default.List
    )
    object Settings : BottomBarScreen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
}