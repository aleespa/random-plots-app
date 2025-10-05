package com.aleespa.randomsquare.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.parkinsansFontFamily


@Composable
fun AspectRatioDialog(
    visualizeModel: VisualizeModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.aspect_ratio_dialog_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    VisualizeOptionsButtons(
                        id = R.drawable.fit,
                        iconSize = 50.dp,
                        bottomText = "Fit"
                    ) {
                        visualizeModel.toFitAspectRatio = true
                        onConfirm()
                    }
                    VisualizeOptionsButtons(
                        id = R.drawable.fill,
                        iconSize = 50.dp,
                        bottomText = "Fill"
                    ) {
                        visualizeModel.toFitAspectRatio = false
                        onConfirm()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}


@Composable
fun FilterTypesDialog(visualizeModel: VisualizeModel) {

    val options = Figures.entries.map { it }.sortedBy { it.key }
    if (visualizeModel.showFilterDialog) {
        AlertDialog(
            onDismissRequest = {
                visualizeModel.showFilterDialog = false
            }, // Close the dialog when clicked outside
            title = {
                Text(
                    text = stringResource(R.string.filter_types),
                    style = TextStyle(
                        fontFamily = parkinsansFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .height(400.dp) // Set the desired height here
                        .fillMaxWidth()
                ) {
                    LazyColumn {
                        item {
                            TextButton(
                                onClick = {
                                    visualizeModel.filterImageType =
                                        "None" // Update the selected option
                                    visualizeModel.showFilterDialog = false // Close the dialog
                                }
                            ) {
                                Text(text = stringResource(R.string.all_images))
                            }
                            HorizontalDivider(thickness = 1.dp)
                        }
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

                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        visualizeModel.showFilterDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}


@Composable
fun VisualizeOptionsButtons(
    id: Int,
    iconSize: Dp = 30.dp,
    bottomText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable {
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Icon(
            painter = painterResource(id = id),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(iconSize)
                .aspectRatio(1f)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = bottomText,
                modifier = Modifier,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}




