package com.example.sawaapplication.ui.screenComponent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sawaapplication.R
import com.example.sawaapplication.ui.theme.firstOrange

@Composable
fun CustomTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        indicator = { tabPositions ->
            SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                height = integerResource(R.integer.extraSmallSpace).dp,
                color = firstOrange
            )
        },
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) }
            ) {
                Box(
                    modifier = Modifier.padding(
                        vertical =integerResource(id = R.integer.smallerSpace).dp,
                        horizontal = integerResource(id = R.integer.mediumSpace).dp
                    )
                ) {
                    Text(
                        text = title,
                        color = if (selectedTabIndex == index) firstOrange else Color.Gray,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}


