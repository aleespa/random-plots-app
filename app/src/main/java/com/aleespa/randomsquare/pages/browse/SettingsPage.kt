package com.aleespa.randomsquare.pages.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.AppThemeSource
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.VisualizeModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.dark_mode),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = SettingDarkMode.values()
                    options.forEachIndexed { index, mode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                visualizeModel.settingDarkMode = mode
                                visualizeModel.bgColor =
                                    0 // Reset bgColor to trigger theme update if necessary
                            },
                            selected = visualizeModel.settingDarkMode == mode,
                            label = {
                                Text(
                                    when (mode) {
                                        SettingDarkMode.Auto -> stringResource(R.string.auto)
                                        SettingDarkMode.On -> stringResource(R.string.on)
                                        SettingDarkMode.Off -> stringResource(R.string.off)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.theme_source),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = AppThemeSource.entries.toTypedArray()
                    options.forEachIndexed { index, source ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { visualizeModel.themeSource = source },
                            selected = visualizeModel.themeSource == source,
                            label = {
                                Text(
                                    when (source) {
                                        AppThemeSource.Device -> stringResource(R.string.device)
                                        AppThemeSource.Image -> stringResource(R.string.image)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.resolution),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val resolutions = listOf(800, 1200, 1800, 2000)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    resolutions.forEachIndexed { index, res ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = resolutions.size
                            ),
                            onClick = { visualizeModel.imageResolution = res },
                            selected = visualizeModel.imageResolution == res,
                            label = { Text(res.toString()) }
                        )
                    }
                }
            }
        }
    }
}
