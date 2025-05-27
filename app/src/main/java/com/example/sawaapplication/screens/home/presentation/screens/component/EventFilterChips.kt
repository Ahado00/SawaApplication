package com.example.sawaapplication.screens.home.presentation.screens.component

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.home.presentation.vmModels.EventFilterType

data class FilterChipItem(
    val type: EventFilterType,
    @StringRes val labelRes: Int
)

@Composable
fun EventFilterChips(
    currentFilter: EventFilterType,
    onFilterSelected: (EventFilterType) -> Unit,
    chips: List<FilterChipItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(integerResource(id= R.integer.smallerSpace).dp),
    ) {
        chips.forEach { chip ->
            val selected = (currentFilter == chip.type)

            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(chip.type) },
                label = { Text(stringResource(chip.labelRes))
                        },
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = MaterialTheme.colorScheme.onTertiary,
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    selectedLabelColor = White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = if (selected)
                        Color.Transparent
                    else
                        MaterialTheme.colorScheme.onTertiary,
                ),
                shape = RoundedCornerShape(integerResource(id= R.integer.roundCardCornerShape).dp)
            )
        }
    }
}
