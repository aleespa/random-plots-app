package com.aleespa.randomsquare.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.data.getBackgroundColorsByType


@Composable
fun BackgroundSelectionDialog(visualizeModel: VisualizeModel) {
    var darkColors = getBackgroundColorsByType("Dark")
    var lightColors = getBackgroundColorsByType("Light")
    val backgroundOptions = if (visualizeModel.isDarkMode) {
        darkColors
    } else {
        lightColors
    }
    if (visualizeModel.showColorDialog) {
        AlertDialog(
            onDismissRequest = { visualizeModel.showColorDialog = false },
            title = {
                Text(text = "Select Background Mode", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                visualizeModel.isDarkMode = false
                                visualizeModel.bgColor = lightColors[0].color
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!visualizeModel.isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = "Light Mode")
                        }
                        Button(
                            onClick = {
                                visualizeModel.isDarkMode = true
                                visualizeModel.bgColor = darkColors[0].color
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (visualizeModel.isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = "Dark Mode")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {


                        itemsIndexed(backgroundOptions) { index, backgroundColor ->
                            if (visualizeModel.bgColor == backgroundColor.color){
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(backgroundColor.color, shape = RoundedCornerShape(8.dp))
                                        .border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            visualizeModel.bgColor = backgroundColor.color
                                        },
                                    contentAlignment = Alignment.Center

                                ){}
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(backgroundColor.color, shape = RoundedCornerShape(8.dp))
                                        .clickable {
                                            visualizeModel.bgColor = backgroundColor.color
                                        },
                                    contentAlignment = Alignment.Center
                                ){}
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { visualizeModel.showColorDialog = false }) {
                    Text(text = "Accept")
                }
            }
        )
    }
}

@Composable
fun AspectRatioDialog(
    visualizeModel: VisualizeModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an aspect ratio") },
        text = {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically)
            {
                VisualizeOptionsButtons(
                    id = R.drawable.fit,
                    iconSize=50.dp,
                    bottomText = "Fit"
                ){
                    visualizeModel.toFitAspectRatio = true
                    onConfirm()
                    }
                VisualizeOptionsButtons(
                    id = R.drawable.fill,
                    iconSize=50.dp,
                    bottomText = "Fill"
                ){
                    visualizeModel.toFitAspectRatio = false
                    onConfirm()
                    }
            }
               }
               },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun FilterTypesDialog(visualizeModel: VisualizeModel) {

    val options = Figures.entries.map { it }
    if (visualizeModel.showFilterDialog) {
        AlertDialog(
            onDismissRequest = {
                visualizeModel.showFilterDialog = false
            }, // Close the dialog when clicked outside
            title = { Text("Figure type") },
            text = {
                LazyColumn {
                    item { HorizontalDivider(thickness = 2.dp) }
                    item {
                        options.forEach { option ->
                            TextButton(
                                onClick = {
                                    visualizeModel.filterImageType =
                                        option.key // Update the selected option
                                    visualizeModel.showFilterDialog = false // Close the dialog
                                }
                            ) {
                                Text(text = stringResource(option.resourceStringId))
                            }
                            HorizontalDivider(thickness = 1.dp)
                        }
                    }
                    item {
                        TextButton(
                            onClick = {
                                visualizeModel.filterImageType =
                                    "None" // Update the selected option
                                visualizeModel.showFilterDialog = false // Close the dialog
                            }
                        ) {
                            Text(text = "All figures")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        visualizeModel.showFilterDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}