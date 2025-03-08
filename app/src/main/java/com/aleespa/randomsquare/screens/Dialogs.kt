package com.aleespa.randomsquare.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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