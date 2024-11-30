package com.aleespa.randomsquare

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.ui.graphics.vector.ImageVector

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