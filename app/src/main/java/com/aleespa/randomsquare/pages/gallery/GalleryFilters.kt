package com.aleespa.randomsquare.pages.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.FilterTypesDialog
import com.aleespa.randomsquare.ui.theme.Spacing

private const val NO_TYPE_FILTER = "None"

@Composable
fun FilterChips(visualizeModel: VisualizeModel) {
    LazyRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.screenGutter),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        item {
            ToggleFilterChip(
                label = stringResource(R.string.dark_name),
                selected = visualizeModel.darkFilter,
                onClick = {
                    visualizeModel.darkFilter = !visualizeModel.darkFilter
                    if (visualizeModel.darkFilter) visualizeModel.lightFilter = false
                }
            )
        }
        item {
            ToggleFilterChip(
                label = stringResource(R.string.light_name),
                selected = visualizeModel.lightFilter,
                onClick = {
                    visualizeModel.lightFilter = !visualizeModel.lightFilter
                    if (visualizeModel.lightFilter) visualizeModel.darkFilter = false
                }
            )
        }
        item {
            FilterChipWithDropdown(visualizeModel)
        }
    }
}

/** Every selected chip shows the same check icon. */
@Composable
private fun SelectedCheckIcon() {
    Icon(
        imageVector = Icons.Filled.Done,
        contentDescription = null,
        modifier = Modifier.size(FilterChipDefaults.IconSize)
    )
}

@Composable
private fun ToggleFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { SelectedCheckIcon() }
        } else null,
    )
}

@Composable
fun FilterChipWithDropdown(visualizeModel: VisualizeModel) {
    val selected = visualizeModel.filterImageType != NO_TYPE_FILTER
    FilterChip(
        selected = selected,
        onClick = { visualizeModel.showFilterDialog = true },
        label = {
            if (selected) {
                Text(stringResource(Figures.fromKey(visualizeModel.filterImageType).resourceStringId))
            } else {
                Text(text = stringResource(R.string.filter_types))
            }
        },
        leadingIcon = if (selected) {
            { SelectedCheckIcon() }
        } else null,
        // Always shown: this chip opens a picker rather than toggling.
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        }
    )
    FilterTypesDialog(visualizeModel)
}
