package com.aleespa.randomsquare.pages

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
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

private val topLevelScreens = listOf(
    BottomBarScreen.Browse,
    BottomBarScreen.Visualize,
    BottomBarScreen.Gallery
)

/**
 * App shell. Shows a bottom navigation bar on compact windows (phones) and a navigation rail on
 * medium/expanded windows (tablets, unfolded foldables). The scaffold lays the screens out next
 * to the navigation component and consumes its insets, so screens need no extra bottom padding.
 */
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val navigationContainerColor = MaterialTheme.colorScheme.surface
    // Material defaults the selected label to `secondary`, which in the light scheme is a pale
    // gray — lighter than the unselected labels. Use the strongest text color instead.
    val selectedItemContentColor = MaterialTheme.colorScheme.onSurface
    val navigationItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedItemContentColor,
            selectedTextColor = selectedItemContentColor,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = selectedItemContentColor,
            selectedTextColor = selectedItemContentColor,
        ),
    )

    NavigationSuiteScaffold(
        modifier = Modifier.blur(blurRadius),
        layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfoV2()
        ),
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = navigationContainerColor,
            navigationRailContainerColor = navigationContainerColor,
        ),
        navigationSuiteItems = {
            topLevelScreens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any {
                    it.route == screen.route
                } == true
                item(
                    selected = isSelected,
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(stringResource(id = screen.titleResourceId)) },
                    colors = navigationItemColors,
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
    ) {
        BottomNavGraph(
            visualizeModel = remember { visualizeModel },
            navController = navController
        )
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
