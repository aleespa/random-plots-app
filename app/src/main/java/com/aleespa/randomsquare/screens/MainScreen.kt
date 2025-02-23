package com.aleespa.randomsquare.screens
import android.annotation.SuppressLint
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
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
import androidx.compose.runtime.remember
import com.google.android.gms.ads.interstitial.InterstitialAd

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(visualizeModel: VisualizeModel) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {BottomBar(navController)}
    ) {
        BottomNavGraph(
            visualizeModel = remember {visualizeModel},
            navController = navController)
    }
}

@Composable
fun BottomBar(navController: NavHostController){
    val screens = listOf(
        BottomBarScreen.Browse,
        BottomBarScreen.Visualize,
        BottomBarScreen.Gallery
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = NavigationBarDefaults.containerColor
    )
    {
        screens.forEach  { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(id = screen.titleResourceId)) },
                selected = currentDestination?.hierarchy?.any{
                    it.route == screen.route
                } == true,
                onClick = { navController.navigate(screen.route){
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                } }
            )
        }

    }
}


@Composable
fun BottomNavGraph(visualizeModel: VisualizeModel = viewModel(),
                   navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Browse.route
    ) {
        composable(route = BottomBarScreen.Browse.route) {
            Browse(visualizeModel, navController)
        }
        composable(route = BottomBarScreen.Visualize.route) {
            Visualize(visualizeModel, navController)
        }
        composable(route = BottomBarScreen.Gallery.route) {
            Gallery(visualizeModel, navController)
        }
    }
}