package com.alejandro.randomplots

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen (
    val route: String,
    val titleResourceId: Int,
    val icon: ImageVector
){
    data object Customize : BottomBarScreen(
        route = "customize",
        titleResourceId = R.string.customize,
        icon = Icons.Default.Build
    )
    data object Visualize : BottomBarScreen(
        route = "create",
        titleResourceId = R.string.visualize,
        icon = Icons.Default.Create
    )
    data object Gallery : BottomBarScreen(
        route = "gallery",
        titleResourceId = R.string.gallery,
        icon = Icons.Default.Menu
    )

}