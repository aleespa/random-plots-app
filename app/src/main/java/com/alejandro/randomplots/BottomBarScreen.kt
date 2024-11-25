package com.alejandro.randomplots

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class BottomBarScreen (
    val route: String,
    val titleResourceId: Int,
    val icon: ImageVector

){
    data object Visualize : BottomBarScreen(
        route = "visualize",
        titleResourceId = R.string.visualize,
        icon = Icons.Default.AutoFixHigh
    )
    data object Gallery : BottomBarScreen(
        route = "gallery",
        titleResourceId = R.string.gallery,
        icon = Icons.Default.Apps
    )

}