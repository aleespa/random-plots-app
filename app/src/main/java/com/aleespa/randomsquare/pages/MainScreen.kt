package com.aleespa.randomsquare.pages

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.browse.Browse
import com.aleespa.randomsquare.pages.browse.SettingsPage
import com.aleespa.randomsquare.pages.gallery.Gallery
import com.aleespa.randomsquare.pages.visualize.Visualize

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    visualizeModel: VisualizeModel
) {
    val navController = rememberNavController()
    val isAnyDialogOpen = visualizeModel.showFilterDialog ||
            visualizeModel.showAspectRatioDialog ||
            visualizeModel.showDeleteAllDialog ||
            visualizeModel.showColormapDialog

    val blurRadius by animateDpAsState(
        targetValue = if (isAnyDialogOpen) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "BlurAnimation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.blur(blurRadius),
            bottomBar = { BottomBar(navController, visualizeModel) }
        ) {
            BottomNavGraph(
                visualizeModel = remember { visualizeModel },
                navController = navController
            )
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController, visualizeModel: VisualizeModel) {

    val screens = listOf(
        BottomBarScreen.Browse,
        BottomBarScreen.Visualize,
        BottomBarScreen.Gallery
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == screen.route
            } == true
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(id = screen.titleResourceId)) },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        when (screen) {
                            BottomBarScreen.Browse -> visualizeModel.scrollToTopBrowse++
                            BottomBarScreen.Gallery -> visualizeModel.scrollToTopGallery++
                            else -> {}
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun BottomNavGraph(
    visualizeModel: VisualizeModel = viewModel(),
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Browse.route,
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(route = BottomBarScreen.Browse.route) {
            Browse(visualizeModel, navController)
        }
        composable(route = "settings") {
            SettingsPage(visualizeModel, navController)
        }
        composable(route = BottomBarScreen.Visualize.route) {
            Visualize(visualizeModel, navController)
        }
        composable(route = BottomBarScreen.Gallery.route) {
            Gallery(visualizeModel, navController)
        }
    }
}
