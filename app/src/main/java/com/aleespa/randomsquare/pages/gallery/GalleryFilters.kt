package com.aleespa.randomsquare.pages.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.FilterTypesDialog

@Composable
fun FilterChips(visualizeModel: VisualizeModel) {
    LazyRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
    ) {
        item {
            FilterChip(
                modifier = Modifier
                    .padding(start = 10.dp),
                onClick = {
                    if (visualizeModel.darkFilter) {
                        visualizeModel.darkFilter = false
                    } else {
                        visualizeModel.darkFilter = true
                        visualizeModel.lightFilter = false
                    }
                },
                label = { Text(stringResource(R.string.dark_name)) },
                selected = visualizeModel.darkFilter,
                leadingIcon = if (visualizeModel.darkFilter) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
            )
        }
        item {
            FilterChip(
                modifier = Modifier
                    .padding(start = 10.dp),
                onClick = {
                    if (visualizeModel.lightFilter) {
                        visualizeModel.lightFilter = false
                    } else {
                        visualizeModel.lightFilter = true
                        visualizeModel.darkFilter = false
                    }
                },
                label = { Text(stringResource(R.string.light_name)) },
                selected = visualizeModel.lightFilter,
                leadingIcon = if (visualizeModel.lightFilter) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
            )
        }
        item {
            FilterChipWithDropdown(visualizeModel)
        }
    }
}


@Composable
fun FilterChipWithDropdown(visualizeModel: VisualizeModel) {
    FilterChip(
        selected = (visualizeModel.filterImageType != "None"),
        onClick = { visualizeModel.showFilterDialog = true },
        label = {
            if (visualizeModel.filterImageType == "None") {
                Text(text = stringResource(R.string.filter_types))
            } else {
                Text(stringResource(Figures.fromKey(visualizeModel.filterImageType).resourceStringId))
            }
        },
        modifier = Modifier.padding(start = 10.dp),
        trailingIcon = if (visualizeModel.filterImageType == "None") {
            {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        }
    )
    FilterTypesDialog(visualizeModel)
}
