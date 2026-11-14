package com.aleespa.randomsquare.pages

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.aleespa.randomsquare.data.AppLaunch
import com.aleespa.randomsquare.tools.openRandomFigure
import com.aleespa.randomsquare.tools.openRecipe
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
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

    val context = LocalContext.current
    LaunchedEffect(visualizeModel.pendingLaunch) {
        val launch = visualizeModel.pendingLaunch ?: return@LaunchedEffect
        visualizeModel.pendingLaunch = null
        visualizeModel.awaitSettingsLoaded()
        val route = when (launch) {
            AppLaunch.RandomFigure -> {
                openRandomFigure(visualizeModel, context)
                BottomBarScreen.Visualize.route
            }
            is AppLaunch.Recipe -> {
                openRecipe(visualizeModel, launch.image, context)
                BottomBarScreen.Visualize.route
            }
            AppLaunch.Gallery -> BottomBarScreen.Gallery.route
            AppLaunch.Settings -> "settings"
        }
        navController.navigate(route) { launchSingleTop = true }
    }

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


/** Screens that open into Visualize with the shared image transition (see [sharedImage]). */
private val imageOrigins = setOf(BottomBarScreen.Browse.route, BottomBarScreen.Gallery.route)

@Composable
fun BottomNavGraph(
    visualizeModel: VisualizeModel = viewModel(),
    navController: NavHostController
) {
    val visualize = BottomBarScreen.Visualize.route
    // Between Visualize and Browse/Gallery the image flies between thumbnail and frame
    // (SHARED_IMAGE_MS); the rest of the leaving screen fades out quickly and the arriving one
    // fades in behind the moving image. Everything else keeps a short crossfade. Navigation runs
    // the pop transitions under the predictive back gesture.
    fun isImageHop(from: NavBackStackEntry, to: NavBackStackEntry): Boolean {
        val a = from.destination.route
        val b = to.destination.route
        return (a in imageOrigins && b == visualize) || (a == visualize && b in imageOrigins)
    }
    fun fadeInFor(from: NavBackStackEntry, to: NavBackStackEntry) =
        if (isImageHop(from, to)) fadeIn(tween(SHARED_IMAGE_MS - 150, delayMillis = 100))
        else fadeIn(tween(200))
    // Opening Visualize, Browse/Gallery stays put until Visualize has faded in over it, so the
    // screen never washes out between the two.
    fun holdThenFadeOutFor(from: NavBackStackEntry, to: NavBackStackEntry) =
        if (isImageHop(from, to)) fadeOut(tween(100, delayMillis = SHARED_IMAGE_MS - 150))
        else fadeOut(tween(200))
    // Going back, the screen underneath is already in place (as Android previews it), so only
    // Visualize fades and nothing washes out mid-gesture.
    fun popFadeInFor(from: NavBackStackEntry, to: NavBackStackEntry) =
        if (isImageHop(from, to)) EnterTransition.None else fadeIn(tween(200))
    fun fadeOutFor(from: NavBackStackEntry, to: NavBackStackEntry) =
        if (isImageHop(from, to)) fadeOut(tween(250)) else fadeOut(tween(200))
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = BottomBarScreen.Browse.route,
                enterTransition = { fadeInFor(initialState, targetState) },
                exitTransition = { holdThenFadeOutFor(initialState, targetState) },
                popEnterTransition = { popFadeInFor(initialState, targetState) },
                popExitTransition = { fadeOutFor(initialState, targetState) },
                // Navigation's default for the back gesture shrinks the leaving screen to 70%;
                // here it fades in place while the shared image flies back to its thumbnail.
                predictivePopEnterTransition = { popFadeInFor(initialState, targetState) },
                predictivePopExitTransition = { fadeOutFor(initialState, targetState) },
            ) {
                composable(route = BottomBarScreen.Browse.route) {
                    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                        Browse(visualizeModel, navController)
                    }
                }
                composable(route = "settings") {
                    SettingsPage(visualizeModel, navController)
                }
                composable(route = visualize) {
                    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                        Visualize(visualizeModel, navController)
                    }
                }
                composable(route = BottomBarScreen.Gallery.route) {
                    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                        Gallery(visualizeModel, navController)
                    }
                }
            }
        }
    }
}
