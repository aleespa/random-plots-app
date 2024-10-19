package com.alejandro.randomplots.screens
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alejandro.randomplots.BottomBarScreen
import com.alejandro.randomplots.R
import com.alejandro.randomplots.tools.PlotSettings
import com.alejandro.randomplots.tools.SettingsViewModel
import com.alejandro.randomplots.tools.generateRandomPlot
import com.alejandro.randomplots.tools.saveBitmapToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Customize(navController: NavHostController) {
    val viewModel: SettingsViewModel = viewModel()
    val options = listOf("spirograph", "cont_spirograph")
    var selectedOption by remember { mutableStateOf(options[0]) }
    val settings = PlotSettings(10, "shape", 10.0f)
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    viewModel.setSettings(settings)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        selectedOption = dropdownMenu(options)
        Text(
            text = selectedOption,
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentSize()
        )
        ExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            elevation = FloatingActionButtonDefaults.elevation(10.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    val result = generateRandomPlot(isDarkTheme, selectedOption)
                    val androidBitmap = result.first?.asAndroidBitmap()
                    if (androidBitmap != null) {
                        saveBitmapToFile(context, androidBitmap,
                            "cache_front.png")
                    }
                }
            }
        ) {
            Text(
                text = stringResource(id = R.string.generate),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun dropdownMenu(options: List<String>): String {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }
    Box(modifier = Modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Select a style")
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Open menu")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                    }
                )
                HorizontalDivider()
            }

        }
    }
    return selectedOption
}