package com.example.sawaapplication.screens.communities.presentation.screens.component

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sawaapplication.R
import com.example.sawaapplication.ui.theme.PrimaryOrange

@Composable
fun CommunityTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(stringResource(R.string.posts), stringResource(R.string.events))

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.background,
        indicator = { positions ->
            TabRowDefaults.Indicator(
                Modifier
                    .tabIndicatorOffset(positions[selectedTab])
                    .height(integerResource(R.integer.tabRowHeight).dp),
                color = PrimaryOrange
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedTab == index) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            )
        }
    }
}