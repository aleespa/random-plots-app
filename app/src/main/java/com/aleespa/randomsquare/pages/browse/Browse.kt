package com.aleespa.randomsquare.pages.browse

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.VisualizeModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Browse(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    BackHandler {
        (context as? Activity)?.finish()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {},
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (visualizeModel.settingDarkMode) {
                                SettingDarkMode.On -> ""
                                SettingDarkMode.Off -> ""
                                SettingDarkMode.Auto -> stringResource(id = R.string.auto)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(onClick = {
                            visualizeModel.settingDarkMode = when (visualizeModel.settingDarkMode) {
                                SettingDarkMode.Auto -> SettingDarkMode.On
                                SettingDarkMode.On -> SettingDarkMode.Off
                                SettingDarkMode.Off -> SettingDarkMode.Auto
                            }
                            visualizeModel.bgColor = 0
                        }) {
                            Icon(
                                imageVector = when (visualizeModel.settingDarkMode) {
                                    SettingDarkMode.Auto -> Icons.Default.DarkMode
                                    SettingDarkMode.On -> Icons.Default.DarkMode
                                    SettingDarkMode.Off -> Icons.Default.LightMode
                                },
                                contentDescription = "Change dark mode",
                                modifier = Modifier.size(35.dp)
                            )
                        }
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
