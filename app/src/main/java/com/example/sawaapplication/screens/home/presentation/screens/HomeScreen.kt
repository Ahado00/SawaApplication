package com.example.sawaapplication.screens.home.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.ui.screenComponent.CustomTabRow
import com.example.sawaapplication.screens.home.presentation.screens.component.MyEventsTab
import com.example.sawaapplication.screens.home.presentation.screens.component.PostsTab
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.posts), stringResource(R.string.events))

    Box(modifier = Modifier.fillMaxSize()) {

        when (selectedTabIndex) {
            0 -> PostsTab(viewModel, navController)
            1 -> MyEventsTab(navController = navController)
        }

        // Top transparent tab row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(integerResource(id = R.integer.homeScreenPadding).dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
        ) {
            CustomTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
        }
    }
}
