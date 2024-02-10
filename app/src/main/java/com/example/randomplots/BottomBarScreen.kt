package com.example.randomplots

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen (
    val route: String,
    val titleResourceId: Int,
    val icon: ImageVector
){
    object Create : BottomBarScreen(
        route = "create",
        titleResourceId = R.string.create,
        icon = Icons.Default.Create
    )
    object Gallery : BottomBarScreen(
        route = "gallery",
        titleResourceId = R.string.gallery,
        icon = Icons.Default.Menu
    )
    object Settings : BottomBarScreen(
        route = "settings",
        titleResourceId = R.string.settings,
        icon = Icons.Default.Settings
    )
}