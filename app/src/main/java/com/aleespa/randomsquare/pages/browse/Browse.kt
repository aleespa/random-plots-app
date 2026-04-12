package com.aleespa.randomsquare.pages.browse

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.data.VisualizeModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Browse(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    BackHandler {
        (context as? Activity)?.finish()
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                expandedHeight = 28.dp,
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {},
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("settings")
                        },
                        modifier = Modifier.graphicsLayer {
                            val fraction = scrollBehavior.state.collapsedFraction
                            rotationZ = fraction * 120f
                            scaleX = 1f - (fraction * 0.7f)
                            scaleY = 1f - (fraction * 0.7f)
                            alpha = 1f - (fraction * 0.7f)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BrowserScrollable(
            visualizeModel,
            innerPadding,
            navController,
            context
        )
    }
}
