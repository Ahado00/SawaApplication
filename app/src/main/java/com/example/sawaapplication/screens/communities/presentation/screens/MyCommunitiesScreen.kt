package com.example.sawaapplication.screens.communities.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.communities.presentation.screens.component.MyCommunitiesCard
import com.example.sawaapplication.screens.communities.presentation.vmModels.CommunityViewModel
import com.example.sawaapplication.ui.screenComponent.buttons.FloatingButton
import com.example.sawaapplication.ui.screenComponent.SearchField
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MyCommunitiesScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Observe the state of communities and loading
    val communities by viewModel.createdCommunities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val searchText by viewModel.searchText.collectAsState()
    val filteredCommunities by viewModel.filteredCreatedCommunities.collectAsState()

    // Fetch the communities when the screen is loaded
    LaunchedEffect(Unit) {
        viewModel.fetchCreatedCommunities(currentUserId)
    }
    Column {
        SearchField(
            value = searchText,
            onValueChange = viewModel::onSearchTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = integerResource(R.integer.padding).dp)
        )
        Spacer(Modifier.height(integerResource(R.integer.mediumSpace).dp))
        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(integerResource(id = R.integer.smallerSpace).dp),
                verticalArrangement = Arrangement.spacedBy(integerResource(id = R.integer.smallerSpace).dp),
                horizontalArrangement = Arrangement.spacedBy(integerResource(id = R.integer.smallerSpace).dp),
                modifier = Modifier.padding(vertical = integerResource(id = R.integer.largerSpace).dp)
            ) {
                items(filteredCommunities) { community ->
                    MyCommunitiesCard(
                        community = community,
                        onClick = {
                            // Added debug log to confirm the community ID being navigated to
                            Log.d("DEBUG", "Navigating to community id: ${community.id}")
                            // Navigate to the CommunityScreen using the community ID
                            navController.navigate("community_screen/${community.id}")
                        }
                    )
                }
            }
            FloatingButton(
                onClick = { navController.navigate(Screen.NewCommunity.route) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(integerResource(R.integer.mediumSpace).dp)
            )
        }
    }
}